package de.regatta_hd.aquarius.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.SetListEntry;
import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.Crew;
import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Race.GroupMode;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.Score;
import de.regatta_hd.common.ConfigService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@Singleton
public class RegattaDAOImpl extends AbstractDAOImpl implements RegattaDAO {
	private static final Logger logger = Logger.getLogger(RegattaDAOImpl.class.getName());

	private static final String PARAM_RACE_NUMBER = "number";

	private static final String PARAM_REGATTA = "regatta";

	private static final String ACTIVE_REGATTA = "activeRegatta";

	@Inject
	private ConfigService cfgService;

	private int activeRegattaId = -1;

	@Override
	public List<Regatta> getRegattas() {
		return getEntities(Regatta.class);
	}

	public List<Race> getRaces() {
		return getRaces(null);
	}

	public List<Race> getRaces(String graphName) {
		TypedQuery<Race> query = this.db.getEntityManager()
				.createQuery("SELECT r FROM Race r WHERE r.regatta = :regatta", Race.class)
				.setParameter(PARAM_REGATTA, getActiveRegatta());
		if (graphName != null) {
			EntityGraph<?> entityGraph = this.db.getEntityManager().getEntityGraph(graphName);
			query.setHint("javax.persistence.fetchgraph", entityGraph);
		}
		return query.getResultList();
	}

	public Race getRace(String raceNumber) {
		return getRace(raceNumber, null);
	}

	@Override
	public Race getRace(String raceNumber, String graphName) {
		EntityManager entityManager = super.db.getEntityManager();

		TypedQuery<Race> query = entityManager
				.createQuery("SELECT r FROM Race r WHERE r.regatta = :regatta AND r.number = :number", Race.class)
				.setParameter(PARAM_REGATTA, getActiveRegatta()).setParameter(PARAM_RACE_NUMBER, raceNumber);
		if (graphName != null) {
			EntityGraph<?> entityGraph = entityManager.getEntityGraph(graphName);
			query.setHint("javax.persistence.fetchgraph", entityGraph);
		}
		return query.getSingleResult();
	}

	@Override
	public void setRaceHeats(Race race, List<SetListEntry> setList) {
		int numRegistrations = setList.size();
		short laneCount = race.getRaceMode().getLaneCount();

		// get all planed heats ordered by the heat number
		List<Heat> heats = race.getHeatsOrderedByNumber();

		// get number of heats
		int heatCount = heats.size();

		EntityManager entityManager = super.db.getEntityManager();

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
			return super.db.getEntityManager().find(Regatta.class, Integer.valueOf(this.activeRegattaId));
		} catch (IOException e) {
			logger.log(Level.SEVERE, e, null);
		}
		return null;
	}

	public void cleanRaceHeats(Race race) {
		EntityManager entityManager = super.db.getEntityManager();

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
		Map<Integer, SetListEntry> equalCrews = new HashMap<>();
		Map<Integer, SetListEntry> diffCrews = new HashMap<>();

		Set<Registration> srcRegistrations = ConcurrentHashMap.newKeySet();
		srcRegistrations.addAll(srcRace.getRegistrations());

		// loop over all registrations of race to be set
		for (Registration registration : race.getRegistrations()) {
			SetListEntry entry = SetListEntry.builder().registration(registration).equalCrew(false).build();
			diffCrews.put(registration.getId(), entry);

			for (Registration srcRegistration : srcRegistrations) {
				// look for equal crew in the source registration
				if (isEqualCrews(srcRegistration, registration)) {
					SetListEntry equalCrewEntry = diffCrews.remove(registration.getId());
					// mark crews as equal
					equalCrewEntry.setEqualCrew(true);
					// put entry into map of equal crews
					equalCrews.put(srcRegistration.getId(), equalCrewEntry);

					// source registration assigned, remove it from further processing
					srcRegistrations.remove(srcRegistration);
				}
			}
		}

		List<List<HeatRegistration>> srcHeatRegsAll = getSrcHeatsByRank(srcRace);

		List<SetListEntry> setList = new ArrayList<>();
		for (List<HeatRegistration> srcHeatRegs : srcHeatRegsAll) {
			srcHeatRegs.stream().sorted((heatReg1, heatReg2) -> {
				if (heatReg1.getFinalResult() == null || heatReg2.getFinalResult() == null) {
					return 0;
				}
				return heatReg1.getFinalResult().getNetTime().intValue() > heatReg2.getFinalResult().getNetTime()
						.intValue() ? 1 : -1;
			}).forEach(srcHeatReg -> {
				SetListEntry targetRegistration = equalCrews.get(srcHeatReg.getRegistration().getId());
				if (targetRegistration != null) {
					setList.add(SetListEntry.builder().rank(setList.size() + 1).heatRregistration(srcHeatReg)
							.registration(targetRegistration.getRegistration()).equalCrew(true).build());
				}
			});
		}

		// sort the different crews according their number
		diffCrews.values().stream().sorted((reg1, reg2) -> reg1.getBib() > reg2.getBib() ? 1 : -1)
				.forEach(setListEntry -> setList.add(SetListEntry.builder().rank(setList.size() + 1)
						.registration(setListEntry.getRegistration()).equalCrew(false).build()));

		return setList;
	}

	private static List<List<HeatRegistration>> getSrcHeatsByRank(Race srcRace) {
		List<List<HeatRegistration>> srcHeatRegs = new ArrayList<>();

		for (int i = 0; i < srcRace.getRaceMode().getLaneCount(); i++) {
			srcHeatRegs.add(new ArrayList<>());
		}

		// loop over source offer heats and get all heat registrations sorted by time
		srcRace.getHeats().forEach(heat -> {
			List<HeatRegistration> byRank = heat.getHeatRegistrationsOrderedByRank();
			for (int j = 0; j < byRank.size(); j++) {
				srcHeatRegs.get(j).add(byRank.get(j));
			}
		});
		return srcHeatRegs;
	}

	@Override
	public List<Score> calculateScores() {
		EntityManager entityManager = this.db.getEntityManager();

		// first clear persistence context
		entityManager.clear();

		Map<Club, Score> scores = new HashMap<>();
		Regatta regatta = getActiveRegatta();

		for (Race race : getRaces("race-to-results")) {
			if (race.isOfficial()) {
				short laneCount = race.getRaceMode().getLaneCount();
				byte numRowers = race.getBoatClass().getNumRowers();
				if (race.getBoatClass().isCoxed()) {
					numRowers++;
				}

				for (Heat heat : race.getHeats()) {
					for (HeatRegistration heatReg : heat.getEntries()) {
						byte rank = heatReg.getFinalResult() != null ? heatReg.getFinalResult().getRank() : 0;

						if (rank > 0) {
							float pointsBoat = (numRowers * (laneCount + 1 - rank));
							float pointsPerCrew = pointsBoat / numRowers;

							heatReg.getRegistration().getCrews().forEach(crew -> {
								Score score = scores.computeIfAbsent(crew.getClub(),
										key -> Score.builder().club(key).regatta(regatta).points(0).build());
								score.getClubName();
								score.addPoints(pointsPerCrew);
							});
						}
					}
				}
			}
		}

		return updateScores(scores.values(), entityManager);
	}

	@Override
	public List<Score> getScores() {
		EntityManager entityManager = this.db.getEntityManager();

		// first clear persistence context
		entityManager.clear();

		return entityManager
				.createQuery("SELECT s FROM Score s WHERE s.regatta = :regatta ORDER BY s.rank ASC", Score.class)
				.setParameter(PARAM_REGATTA, getActiveRegatta()).getResultList();
	}

	private List<Score> updateScores(Collection<Score> scores, EntityManager entityManager) {
		entityManager.createQuery("DELETE FROM Score s WHERE s.regatta = :regatta")
				.setParameter(PARAM_REGATTA, getActiveRegatta()).executeUpdate();

		List<Score> scoresResult = scores.stream().sorted((score1, score2) -> {
			if (score1.getPoints() == score2.getPoints()) {
				return 0;
			}
			return score1.getPoints() > score2.getPoints() ? -1 : 1;
		}).collect(Collectors.toList());

		for (int i = 0; i < scoresResult.size(); i++) {
			Score score = scoresResult.get(i);
			score.setRank((short) (i + 1));
			entityManager.persist(score);
		}

		entityManager.flush();

		return scoresResult;
	}

	// static helpers

	private static boolean isEqualCrews(Registration reg1, Registration reg2) {
		// remove cox from comparison
		List<Crew> crews1 = reg1.getCrews().stream().filter(crew -> !crew.isCox()).sorted(RegattaDAOImpl::compare)
				.collect(Collectors.toList());
		List<Crew> crews2 = reg2.getCrews().stream().filter(crew -> !crew.isCox()).sorted(RegattaDAOImpl::compare)
				.collect(Collectors.toList());

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

	@Override
	public List<Race> enableMastersAgeClasses() {
		List<Race> races = new ArrayList<>();

		EntityManager entityManager = this.db.getEntityManager();

		getRaces().forEach(race -> {
			AgeClass ageClass = race.getAgeClass();
			GroupMode mode = race.getGroupMode();
			if (ageClass.isMasters() && !mode.equals(GroupMode.AGE)) {
				race.setGroupMode(GroupMode.AGE);
				entityManager.merge(race);
				races.add(race);
			}
		});

		return races;

	}

	@Override
	public List<Race> setDistances() {
		List<Race> races = new ArrayList<>();

		EntityManager entityManager = this.db.getEntityManager();

		getRaces().forEach(race -> {
			short distance = race.getAgeClass().getDistance();
			if (distance != race.getDistance()) {
				race.setDistance(distance);
				entityManager.merge(race);
				races.add(race);
			}
		});

		return races;
	}
}
