package de.regatta_hd.aquarius.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import de.regatta_hd.aquarius.model.Offer;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.common.ConfigService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Root;

@Singleton
public class RegattaDAOImpl extends AbstractDAOImpl implements RegattaDAO {

	private static final String PARAM_RACE_NUMBER = "raceNumber";

	private static final String PARAM_REGATTA = "regatta";

	private static final String ACTIVE_REGATTA = "activeRegatta";

	@Inject
	private ConfigService cfgService;

	private int activeRegattaId = -1;

	@Override
	public List<Regatta> getRegattas() {
		return getEntities(Regatta.class);
	}

	@Override
	public Offer getOffer(String raceNumber) {
		var critBuilder = getCriteriaBuilder();

		// SELECT o FROM Offer o WHERE o.raceNumber == :nr
		CriteriaQuery<Offer> query = critBuilder.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

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
	public List<Offer> findOffers(String raceNumberFilter, BoatClass boatClass, AgeClass ageClass,
			boolean lightweight) {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Offer> query = critBuilder.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

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
		List<Crew> crews1 = reg1.getCrews();
		List<Crew> crews2 = reg2.getCrews();
		if (crews1.size() != crews2.size()) {
			return false;
		}
		Comparator<Crew> posComparator = (c1, c2) -> {
			if (c1.getAthlet().getId() == c2.getAthlet().getId()) {
				return 0;
			}
			return c1.getAthlet().getId() > c2.getAthlet().getId() ? 1 : -1;
		};
		Collections.sort(crews1, posComparator);

		for (int i = 0; i < crews1.size(); i++) {
			Crew crew1 = crews1.get(i);
			Crew crew2 = crews2.get(i);
			if (crew1.getAthlet().getId() != crew2.getAthlet().getId()) {
				return false;
			}
		}
		return true;
	}

	private List<Registration> getTargetRegistrationsOrdered(Offer targetOffer, Offer sourceOffer) {
		Map<Integer, Registration> sameCrews = new HashMap<>();

		for (Registration targetRegistration : targetOffer.getRegistrations()) {
			for (Registration sourceRegistration : sourceOffer.getRegistrations()) {
				if (isSameCrew(sourceRegistration, targetRegistration)) {
					sameCrews.put(sourceRegistration.getId(), targetRegistration);
				}
			}
		}

		List<Registration> targetRegWinner = new ArrayList<>();
		List<Registration> targetRegOrdered = new ArrayList<>();

		// loop over source offer heats
		List<Heat> sourceHeats = sourceOffer.getHeats();
		for (int i = 0; i < sourceHeats.size(); i++) {
			Heat heat = sourceHeats.get(i);

			List<HeatRegistration> byRank = heat.getHeatRegistrationsOrderedByRank();

			HeatRegistration winner = byRank.get(0);
			Registration targetRegistration = sameCrews.get(winner.getRegistration().getId());
			if (targetRegistration != null) {
				targetRegWinner.add(targetRegistration);
			}

			for (int j = 1; j < byRank.size(); j++) {
				targetRegistration = sameCrews.get(winner.getRegistration().getId());
				if (targetRegistration != null) {
					targetRegOrdered.add(targetRegistration);
				}
			}
		}

		List<Registration> targetRegAll = new ArrayList<>();
		targetRegAll.addAll(targetRegWinner);
		targetRegAll.addAll(targetRegOrdered);
		return targetRegAll;
	}

	@Override
	public void setRace(Offer targetOffer, Offer sourceOffer) {

		List<Registration> targetRegAll = getTargetRegistrationsOrdered(targetOffer, sourceOffer);

		short laneCount = targetOffer.getRaceMode().getLaneCount();
		int numRegistrations = targetRegAll.size();
		short numHeats = (short) ((numRegistrations / laneCount) + (numRegistrations % laneCount == 0 ? 0 : 1));
		List<Heat> targetHeats = targetOffer.getHeatsOrderedByNumber();

		EntityManager entityManager = super.aquariusDb.getEntityManager();
		entityManager.getTransaction().begin();

		for (short heatNumber = 0; heatNumber <= numHeats; heatNumber++) {
			Heat heat = targetHeats.get(heatNumber);
			if (heat != null) {

				int startIndex = heatNumber * laneCount;
				int endIndex = startIndex + laneCount;
				for (int r = startIndex; r < endIndex && r < numRegistrations; r++) {
					Registration targetRegistration = targetRegAll.get(r);

					HeatRegistration heatReg = HeatRegistration.builder().heat(heat).registration(targetRegistration)
							.build();
					entityManager.merge(heatReg);
//					heat.getEntries().add(heatReg);
				}

				entityManager.merge(heat);
			}
		}

		entityManager.getTransaction().commit();
	}

	@Override
	public List<Offer> findOffers(String raceNumber) {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Offer> query = critBuilder.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

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
	public List<Offer> getOffers() {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Offer> query = critBuilder.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

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
}
