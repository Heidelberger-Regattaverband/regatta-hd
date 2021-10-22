package de.regatta_hd.aquarius.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.BoatClass;
import de.regatta_hd.aquarius.model.Crew;
import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.common.ConfigService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Root;

@Singleton
public class RegattaDAOImpl extends AbstractDAOImpl implements RegattaDAO {

	private static final String PARAM_RACE_NUMBER = "number";

	private static final String PARAM_REGATTA = "regatta";

	private static final String ACTIVE_REGATTA = "activeRegatta";

	@Inject
	private ConfigService cfgService;

	private int activeRegattaId = -1;

	public void clear() {
		super.aquariusDb.getEntityManager().clear();
	}

	@Override
	public List<Regatta> getRegattas() {
		return getEntities(Regatta.class);
	}

	@Override
	public Race getOffer(String raceNumber) {
		var critBuilder = getCriteriaBuilder();

		// SELECT o FROM Offer o WHERE o.raceNumber == :nr
		CriteriaQuery<Race> query = critBuilder.createQuery(Race.class);
		Root<Race> o = query.from(Race.class);

		ParameterExpression<Regatta> regattaParam = critBuilder.parameter(Regatta.class, PARAM_REGATTA);
		ParameterExpression<String> raceNumberParam = critBuilder.parameter(String.class, "nr");

		query.where(critBuilder.and( //
				critBuilder.equal(o.get(PARAM_RACE_NUMBER), raceNumberParam), //
				critBuilder.equal(o.get(PARAM_REGATTA), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(raceNumberParam.getName(), requireNonNull(raceNumber, "raceNumber must not be null"))
				.setParameter(regattaParam.getName(),
						requireNonNull(getActiveRegatta(), "activeRegatta must not be null")) //
				.getSingleResult();
	}

	@Override
	public List<Race> findOffers(String raceNumberFilter, BoatClass boatClass, AgeClass ageClass,
			boolean lightweight) {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Race> query = critBuilder.createQuery(Race.class);
		Root<Race> o = query.from(Race.class);

		ParameterExpression<Regatta> regattaParam = critBuilder.parameter(Regatta.class, PARAM_REGATTA);
		ParameterExpression<String> raceNumberParam = critBuilder.parameter(String.class, PARAM_RACE_NUMBER);
		ParameterExpression<BoatClass> boatClassParam = critBuilder.parameter(BoatClass.class, "boatClass");
		ParameterExpression<AgeClass> ageClassParam = critBuilder.parameter(AgeClass.class, "ageClass");
		ParameterExpression<Boolean> lightweightParam = critBuilder.parameter(Boolean.class, "lightweight");

		query.where(critBuilder.and( //
				critBuilder.like(o.get(PARAM_RACE_NUMBER), raceNumberParam),
				critBuilder.equal(o.get("lightweight"), lightweightParam), //
				critBuilder.equal(o.get("boatClass"), boatClassParam), //
				critBuilder.equal(o.get("ageClass"), ageClassParam), //
				critBuilder.equal(o.get(PARAM_REGATTA), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(raceNumberParam.getName(),
						requireNonNull(raceNumberFilter, "raceNumberFilter must not be null"))
				.setParameter(lightweightParam.getName(), lightweight)
				.setParameter(boatClassParam.getName(), requireNonNull(boatClass, "boatClass must not be null"))
				.setParameter(ageClassParam.getName(), requireNonNull(ageClass, "ageClass must not be null"))
				.setParameter(regattaParam.getName(),
						requireNonNull(getActiveRegatta(), "activeRegatta must not be null")) //
				.getResultList();
	}

	private static boolean isSameCrew(Registration reg1, Registration reg2) {
		Comparator<Crew> posComparator = (c1, c2) -> {
			if (c1.getAthlet().getId() == c2.getAthlet().getId()) {
				return 0;
			}
			return c1.getAthlet().getId() > c2.getAthlet().getId() ? 1 : -1;
		};

		// remove cox from comparison
		List<Crew> crews1 = reg1.getCrews().stream().filter(crew -> !crew.isCox()).sorted(posComparator).toList();
		List<Crew> crews2 = reg2.getCrews().stream().filter(crew -> !crew.isCox()).sorted(posComparator).toList();

		if (crews1.size() != crews2.size()) {
			return false;
		}

		for (int i = 0; i < crews1.size(); i++) {
			Crew crew1 = crews1.get(i);
			Crew crew2 = crews2.get(i);
			if (crew1.getAthlet().getId() != crew2.getAthlet().getId()) {
				return false;
			}
		}
		return true;
	}

	private static List<Registration> getTargetRegistrationsOrdered(Race targetOffer, Race sourceOffer) {
		Map<Integer, Registration> sameCrews = new HashMap<>();

		for (Registration targetRegistration : targetOffer.getRegistrations()) {
			for (Registration sourceRegistration : sourceOffer.getRegistrations()) {
				if (isSameCrew(sourceRegistration, targetRegistration)) {
					sameCrews.put(sourceRegistration.getId(), targetRegistration);
				}
			}
		}

		List<List<HeatRegistration>> srcHeatRegsAll = new ArrayList<>();
		for (int i = 0; i < sourceOffer.getRaceMode().getLaneCount(); i++) {
			srcHeatRegsAll.add(new ArrayList<>());
		}

		// loop over source offer heats and get all heat registrations sorted by time
		List<Heat> sourceHeats = sourceOffer.getHeats();
		for (int i = 0; i < sourceHeats.size(); i++) {
			Heat heat = sourceHeats.get(i);

			List<HeatRegistration> byRank = heat.getHeatRegistrationsOrderedByRank();
			for (int j = 0; j < byRank.size(); j++) {
				srcHeatRegsAll.get(j).add(byRank.get(j));
			}
		}

		List<Registration> targetRegsOrdered = new ArrayList<>();
		for (List<HeatRegistration> srcHeatRegs : srcHeatRegsAll) {
			srcHeatRegs.stream().sorted((heatReg1, heatReg2) -> {
				return heatReg1.getFinalResult().getNetTime() > heatReg2.getFinalResult().getNetTime() ? 1 : -1;
			}).forEach(srcHeatReg -> {
				Registration targetRegistration = sameCrews.get(srcHeatReg.getRegistration().getId());
				if (targetRegistration != null) {
					targetRegsOrdered.add(targetRegistration);
				}
			});
		}
		return targetRegsOrdered;
	}

	@Override
	public void assignRace(Race targetOffer, Race sourceOffer) {

		List<Registration> targetRegAll = getTargetRegistrationsOrdered(targetOffer, sourceOffer);

		int numRegistrations = targetRegAll.size();
		short laneCount = targetOffer.getRaceMode().getLaneCount();

		// get all planed heats ordered by the heat number
		List<Heat> targetHeats = targetOffer.getHeatsOrderedByNumber();

		// get number of heats
		int heatCount = targetHeats.size();

		EntityManager entityManager = super.aquariusDb.getEntityManager();
		entityManager.getTransaction().begin();

		for (short heatNumber = 0; heatNumber < heatCount; heatNumber++) {
			Heat heat = targetHeats.get(heatNumber);

			if (heat != null) {
				// first clean existing heat assignments
				heat.getEntries().forEach(entry -> {
					entityManager.remove(entry);
				});

				int startIndex = heatNumber * laneCount;
				int endIndex = startIndex + laneCount;
				short lane = 1;
				for (int r = startIndex; r < endIndex && r < numRegistrations; r++) {
					Registration targetRegistration = targetRegAll.get(r);

					HeatRegistration heatReg = HeatRegistration.builder().heat(heat).registration(targetRegistration)
							.lane(lane++).build();
					entityManager.merge(heatReg);
				}

				entityManager.merge(heat);
			}
		}

		entityManager.getTransaction().commit();

		entityManager.clear();
	}

	@Override
	public List<Race> findOffers(String raceNumber) {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Race> query = critBuilder.createQuery(Race.class);
		Root<Race> o = query.from(Race.class);

		ParameterExpression<Regatta> regattaParam = critBuilder.parameter(Regatta.class, PARAM_REGATTA);
		ParameterExpression<String> raceNumberParam = critBuilder.parameter(String.class, PARAM_RACE_NUMBER);

		query.where(critBuilder.and( //
				critBuilder.like(o.get(PARAM_RACE_NUMBER), raceNumberParam), //
				critBuilder.equal(o.get(PARAM_REGATTA), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(raceNumberParam.getName(), requireNonNull(raceNumber, "raceNumber is null"))
				.setParameter(regattaParam.getName(), requireNonNull(getActiveRegatta(), "activeRegatta is null")) //
				.getResultList();
	}

	@Override
	public List<Race> getOffers() {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Race> query = critBuilder.createQuery(Race.class);
		Root<Race> o = query.from(Race.class);

		ParameterExpression<Regatta> regattaParam = critBuilder.parameter(Regatta.class, PARAM_REGATTA);

		query.where(critBuilder.and( //
				critBuilder.equal(o.get(PARAM_REGATTA), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(regattaParam.getName(), requireNonNull(getActiveRegatta(), "activeRegatta is null")) //
				.getResultList();
	}

	@Override
	public void setActiveRegatta(Regatta regatta) throws IOException {
		if (regatta != null) {
			this.activeRegattaId = regatta.getId();
			this.cfgService.setProperty(ACTIVE_REGATTA, regatta.getId());
		} else {
			this.activeRegattaId = -1;
			this.cfgService.removeProperty(ACTIVE_REGATTA);
		}
	}

	@Override
	public Regatta getActiveRegatta() {
		try {
			if (this.activeRegattaId == -1) {
				String property = this.cfgService.getProperty(ACTIVE_REGATTA);
				if (StringUtils.isNotBlank(property)) {
					this.activeRegattaId = Integer.parseInt(property);
				}
			}
			return find(Regatta.class, this.activeRegattaId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void deleteAssignment(Race race) {
		EntityManager entityManager = super.aquariusDb.getEntityManager();
		entityManager.getTransaction().begin();

		race.getHeats().forEach(heat -> {
			heat.getEntries().forEach(entityManager::remove);
			heat.getEntries().clear();
		});

		entityManager.getTransaction().commit();
	}
}
