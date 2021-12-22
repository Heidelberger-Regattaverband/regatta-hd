package de.regatta_hd.aquarius.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.SetListEntry;
import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.BoatClass;
import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.Crew;
import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.Score;
import de.regatta_hd.common.ConfigService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Root;

@Singleton
public class RegattaDAOImpl extends AbstractDAOImpl implements RegattaDAO {
	private static final Logger logger = Logger.getLogger(RegattaDAOImpl.class.getName());

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
	public Race getRace(String raceNumber) {
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

		return createQuery(query) //
				.setParameter(raceNumberParam.getName(), requireNonNull(raceNumber, "raceNumber must not be null"))
				.setParameter(regattaParam.getName(),
						requireNonNull(getActiveRegatta(), "activeRegatta must not be null")) //
				.getSingleResult();
	}

	@Override
	public List<Race> findRaces(String raceNumberFilter, BoatClass boatClass, AgeClass ageClass, boolean lightweight) {
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

		return createQuery(query) //
				.setParameter(raceNumberParam.getName(),
						requireNonNull(raceNumberFilter, "raceNumberFilter must not be null"))
				.setParameter(lightweightParam.getName(), Boolean.valueOf(lightweight))
				.setParameter(boatClassParam.getName(), requireNonNull(boatClass, "boatClass must not be null"))
				.setParameter(ageClassParam.getName(), requireNonNull(ageClass, "ageClass must not be null"))
				.setParameter(regattaParam.getName(),
						requireNonNull(getActiveRegatta(), "activeRegatta must not be null")) //
				.getResultList();
	}

	@Override
	public void setRaceHeats(Race race, List<SetListEntry> setList) {
		int numRegistrations = setList.size();
		short laneCount = race.getRaceMode().getLaneCount();

		// get all planed heats ordered by the heat number
		List<Heat> heats = race.getHeatsOrderedByNumber();

		// get number of heats
		int heatCount = heats.size();

		EntityManager entityManager = super.aquariusDb.getEntityManager();

		for (short heatNumber = 0; heatNumber < heatCount; heatNumber++) {
			Heat heat = heats.get(heatNumber);

			if (heat != null) {
				// first clean existing heat assignments
				heat.getEntries().forEach(entityManager::remove);

				int startIndex = heatNumber * laneCount;
				int endIndex = startIndex + laneCount;
				short lane = 1;
				for (int r = startIndex; r < endIndex && r < numRegistrations; r++) {
					Registration targetRegistration = setList.get(r).getRegistration();

					HeatRegistration heatReg = HeatRegistration.builder().heat(heat).registration(targetRegistration)
							.lane(lane++).build();
					entityManager.merge(heatReg);
				}

				entityManager.merge(heat);
			}
		}

		// mark race as set
		race.setSet(Boolean.TRUE);
		entityManager.flush();

		entityManager.merge(race);

		entityManager.clear();
	}

	@Override
	public List<Race> findRaces(String raceNumber) {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Race> query = critBuilder.createQuery(Race.class);
		Root<Race> o = query.from(Race.class);

		ParameterExpression<Regatta> regattaParam = critBuilder.parameter(Regatta.class, PARAM_REGATTA);
		ParameterExpression<String> raceNumberParam = critBuilder.parameter(String.class, PARAM_RACE_NUMBER);

		query.where(critBuilder.and( //
				critBuilder.like(o.get(PARAM_RACE_NUMBER), raceNumberParam), //
				critBuilder.equal(o.get(PARAM_REGATTA), regattaParam) //
		));

		return createQuery(query) //
				.setParameter(raceNumberParam.getName(), requireNonNull(raceNumber, "raceNumber is null"))
				.setParameter(regattaParam.getName(), requireNonNull(getActiveRegatta(), "activeRegatta is null")) //
				.getResultList();
	}

	@Override
	public List<Race> getRaces() {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Race> query = critBuilder.createQuery(Race.class);
		Root<Race> o = query.from(Race.class);

		ParameterExpression<Regatta> regattaParam = critBuilder.parameter(Regatta.class, PARAM_REGATTA);

		query.where(critBuilder.and( //
				critBuilder.equal(o.get(PARAM_REGATTA), regattaParam) //
		));

		return createQuery(query) //
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
				this.activeRegattaId = this.cfgService.getIntegerProperty(ACTIVE_REGATTA);
			}
			return find(Regatta.class, Integer.valueOf(this.activeRegattaId));
		} catch (IOException e) {
			logger.log(Level.SEVERE, e, null);
		}
		return null;
	}

	public void cleanRaceHeats(Race race) {
		EntityManager entityManager = super.aquariusDb.getEntityManager();

		race.getHeats().forEach(heat -> {
			heat.getEntries().forEach(entityManager::remove);
			heat.getEntries().clear();
		});

		// mark race as unset
		race.setSet(Boolean.FALSE);
		entityManager.merge(race);
	}

	@Override
	public List<SetListEntry> createSetList(Race race, Race srcRace) {
		Map<Integer, Registration> equalCrews = new HashMap<>();
		Map<Integer, Registration> diffCrews = new HashMap<>();

		for (Registration registration : race.getRegistrations()) {
			diffCrews.put(registration.getId(), registration);

			for (Registration srcRegistration : srcRace.getRegistrations()) {
				if (isEqualCrews(srcRegistration, registration)) {
					equalCrews.put(srcRegistration.getId(), registration);
					diffCrews.remove(registration.getId());
				}
			}
		}

		List<List<HeatRegistration>> srcHeatRegsAll = new ArrayList<>();
		for (int i = 0; i < srcRace.getRaceMode().getLaneCount(); i++) {
			srcHeatRegsAll.add(new ArrayList<>());
		}

		// loop over source offer heats and get all heat registrations sorted by time
		srcRace.getHeats().forEach(heat -> {
			List<HeatRegistration> byRank = heat.getHeatRegistrationsOrderedByRank();
			for (int j = 0; j < byRank.size(); j++) {
				srcHeatRegsAll.get(j).add(byRank.get(j));
			}
		});

		List<SetListEntry> setList = new ArrayList<>();
		for (List<HeatRegistration> srcHeatRegs : srcHeatRegsAll) {
			srcHeatRegs.stream().sorted((heatReg1, heatReg2) -> {
				if (heatReg1.getFinalResult() == null || heatReg2.getFinalResult() == null) {
					return 0;
				}
				return heatReg1.getFinalResult().getNetTime().intValue() > heatReg2.getFinalResult().getNetTime()
						.intValue() ? 1 : -1;
			}).forEach(srcHeatReg -> {
				Registration targetRegistration = equalCrews.get(srcHeatReg.getRegistration().getId());
				if (targetRegistration != null) {
					setList.add(SetListEntry.builder().rank(setList.size() + 1).heatRregistration(srcHeatReg)
							.registration(targetRegistration).equalCrew(true).build());
				}
			});
		}

		diffCrews.values().forEach(registration -> setList.add(
				SetListEntry.builder().rank(setList.size() + 1).registration(registration).equalCrew(false).build()));

		return setList;
	}

	@Override
	public void calculateScores() {
		Map<Club, Score> scores = new HashMap<>();

		List<HeatRegistration> heatRegs = getRaces().stream() //
				.flatMap(race -> (Stream<Heat>) race.getHeats().stream()) //
				.flatMap(heat -> heat.getEntries().stream()).toList();

		heatRegs.forEach(heatReg -> {
			Race race = heatReg.getHeat().getRace();
			short laneCount = race.getRaceMode().getLaneCount();
			byte numRowers = race.getBoatClass().getNumRowers();
			byte rank = heatReg.getFinalResult() != null ? heatReg.getFinalResult().getRank() : 0;
			float points = (numRowers * (laneCount + 1 - rank));

			Club club = heatReg.getRegistration().getClub();

			Score score = scores.computeIfAbsent(club,
					key -> Score.builder().clubId(key.getId()).regattaId(this.activeRegattaId).points(0).build());
			score.addPoints(points);
		});

		updateScores(scores);
	}

	private void updateScores(Map<Club, Score> scores) {
		EntityTransaction transaction = this.aquariusDb.beginTransaction();

		CriteriaDelete<Score> delete = this.aquariusDb.getCriteriaBuilder().createCriteriaDelete(Score.class);
		delete.from(Score.class);
		Query query = this.aquariusDb.getEntityManager().createQuery(delete);
		query.executeUpdate();

		scores.values().forEach(score -> {
			this.aquariusDb.getEntityManager().persist(score);
		});

		this.aquariusDb.getEntityManager().flush();
		transaction.commit();
	}

	// static helpers

	private static boolean isEqualCrews(Registration reg1, Registration reg2) {
		// remove cox from comparison
		List<Crew> crews1 = reg1.getCrews().stream().filter(crew -> !crew.isCox()).sorted(RegattaDAOImpl::compare)
				.toList();
		List<Crew> crews2 = reg2.getCrews().stream().filter(crew -> !crew.isCox()).sorted(RegattaDAOImpl::compare)
				.toList();

		if (crews1.size() != crews2.size()) {
			return false;
		}

		for (int i = 0; i < crews1.size(); i++) {
			if (crews1.get(i).getAthlet().getId() != crews2.get(i).getAthlet().getId()) {
				return false;
			}
		}
		return true;
	}

	private static int compare(Crew crew1, Crew crew2) {
		if (crew1.getAthlet().getId() == crew2.getAthlet().getId()) {
			return 0;
		}
		return crew1.getAthlet().getId() > crew2.getAthlet().getId() ? 1 : -1;
	}
}
