package de.regatta_hd.aquarius.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
import de.regatta_hd.aquarius.ResultEntry;
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
import de.regatta_hd.commons.core.ConfigService;
import de.regatta_hd.commons.core.ListenerManager;
import de.regatta_hd.commons.db.DBConnection;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@Singleton
public class RegattaDAOImpl extends AbstractDAOImpl implements RegattaDAO {
	private static final Logger logger = Logger.getLogger(RegattaDAOImpl.class.getName());
	private static final String PARAM_RACE_NUMBER = "number";
	private static final String PARAM_REGATTA = "regatta";
	private static final String ACTIVE_REGATTA = "activeRegatta";
	private static final String JAVAX_PERSISTENCE_FETCHGRAPH = "javax.persistence.fetchgraph";

	private final ListenerManager listenerManager;

	@Inject
	private ConfigService cfgService;

	private Regatta activeRegatta = null;

	@Inject
	RegattaDAOImpl(ListenerManager listenerManager) {
		this.listenerManager = listenerManager;
		this.listenerManager.addListener(DBConnection.StateChangedEventListener.class, event -> {
			if (event.getDBConnection().isOpen()) {
				getActiveRegatta();
			} else {
				setActiveRegatta(null);
			}
		});
	}

	@Override
	public List<Regatta> getRegattas() {
		List<Regatta> regattas = getEntities(Regatta.class);
		if (this.activeRegatta != null) {
			regattas.forEach(regatta -> regatta.setActive(regatta.getId() == this.activeRegatta.getId()));
		}
		return regattas;
	}

	@Override
	public List<Race> getRaces() {
		return getRaces(null);
	}

	@Override
	public List<Race> getRaces(String graphName) {
		TypedQuery<Race> query = this.db.getEntityManager()
				.createQuery("SELECT r FROM Race r WHERE r.regatta = :regatta ORDER BY r.number ASC", Race.class)
				.setParameter(PARAM_REGATTA, getActiveRegatta());
		if (graphName != null) {
			EntityGraph<?> entityGraph = this.db.getEntityManager().getEntityGraph(graphName);
			query.setHint(JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
		}
		return query.getResultList();
	}

	@Override
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
			query.setHint(JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
		}
		return query.getSingleResult();
	}

	@Override
	public void setRaceHeats(Race race, List<SetListEntry> setList) {
		short laneCount = race.getRaceMode().getLaneCount();

		LinkedList<SetListEntry> stack = new LinkedList<>(setList);
		final EntityManager entityManager = super.db.getEntityManager();

		// get all planed heats ordered by the heat number
		race.getHeatsSortedByDevisionNumber().forEach(heat -> {
			// first clean existing heat assignments
			heat.getEntries().forEach(entityManager::remove);

			for (int lane = 1; lane <= laneCount; lane++) {
				if (!stack.isEmpty()) {
					Registration registration = stack.pop().getRegistration();

					HeatRegistration heatReg = HeatRegistration.builder().heat(heat).registration(registration)
							.lane((short) lane).build();
					entityManager.merge(heatReg);
				} else {
					break;
				}
			}

			// mark heat as set
			heat.setStateSet();
			entityManager.merge(heat);
		});

		// mark race as set
		race.setSet(Boolean.TRUE);
		entityManager.flush();

		entityManager.merge(race);

		entityManager.clear();
	}

	@Override
	public void setActiveRegatta(Regatta regatta) {
		if (regatta != null) {
			this.activeRegatta = regatta;
			try {
				this.cfgService.setProperty(ACTIVE_REGATTA, regatta.getId());
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			this.activeRegatta = null;
		}

		notifyListeners(new RegattaDAORegattaChangedEventImpl(this, this.activeRegatta));
	}

	@Override
	public Regatta getActiveRegatta() {
		if (this.activeRegatta == null) {
			Integer activeRegattaId = this.cfgService.getIntegerProperty(ACTIVE_REGATTA);
			if (activeRegattaId != null) {
				this.activeRegatta = super.db.getEntityManager().find(Regatta.class, activeRegattaId);
			}

			if (this.activeRegatta == null) {
				this.activeRegatta = getRegattas().stream().findFirst().orElse(null);
			}
		}
		return this.activeRegatta;
	}

	@Override
	public void cleanRaceHeats(Race race) {
		EntityManager entityManager = super.db.getEntityManager();

		race.getHeats().forEach(heat -> {
			heat.setState((byte) 0);
			entityManager.merge(heat);
			heat.getEntries().forEach(entityManager::remove);
			heat.getEntries().clear();
		});

		// mark race as unset
		race.setSet(Boolean.FALSE);
		entityManager.merge(race);
	}

	@Override
	public List<SetListEntry> createSetList(Race race, Race srcRace) {
		Map<Integer, SetListEntry> diffCrews = new HashMap<>();

		Set<Registration> srcRegistrations = ConcurrentHashMap.newKeySet();
		srcRegistrations.addAll(srcRace.getRegistrations());

		Map<Integer, SetListEntry> equalCrews = new HashMap<>();

		// remove cancelled registrations
		race.getRegistrations().stream().filter(Registration::isCancelled).forEach(registration -> {
			SetListEntry entry = SetListEntry.builder().registration(registration).equalCrew(false).build();
			diffCrews.put(entry.getId(), entry);

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
					// break current loop and continue with next registration
					break;
				}
			}
		});

		List<SetListEntry> setList = createSetListWithEqualCrews(equalCrews, srcRace);

		// add not added registrations with equal crews, e.g. boat did not finish
		equalCrews.values().forEach(entry -> diffCrews.put(entry.getId(), entry));

		// sort the remaining registrations according their number
		diffCrews.values().stream().sorted((reg1, reg2) -> reg1.getBib() > reg2.getBib() ? 1 : -1).forEach(entry -> {
			entry.setRank(setList.size() + 1);
			findBestMatch(entry, srcRegistrations);
			setList.add(entry);
		});

		return setList;
	}

	@Override
	public List<Score> calculateScores() {
		EntityManager entityManager = this.db.getEntityManager();

		// first clear persistence context
		entityManager.clear();

		Map<Club, Score> scores = new HashMap<>();
		Regatta regatta = getActiveRegatta();

		for (ResultEntry resultEntry : getOfficialResults()) {
			Race race = resultEntry.getHeat().getRace();
			boolean raceIsSet = race.isSet();
			// gets the number of rowers without the cox
			byte numRowers = race.getBoatClass().getNumRowers();

			for (HeatRegistration heatReg : resultEntry.getHeat().getEntries()) {
				if (heatReg.getFinalResult() != null) {
					Integer pointsBoat = heatReg.getFinalResult().getPoints();

					if (pointsBoat != null) {
						// duplicate points if it's the first heat of a set race
						if (raceIsSet && heatReg.getHeat().getDevisionNumber() == 1) {
							pointsBoat = Integer.valueOf(pointsBoat.intValue() * 2);
						}

						float pointsPerCrew = (float) pointsBoat.intValue() / (float) numRowers;

						// ignore cox of boat
						heatReg.getRegistration().getCrews().stream().filter(crew -> !crew.isCox()).forEach(crew -> {
							Score score = scores.computeIfAbsent(crew.getAthlet().getClub(),
									key -> Score.builder().club(key).regatta(regatta).points(0.0f).build());
							score.addPoints(pointsPerCrew);

//						System.out.println("Heat=" + heatReg.getHeat().getNumber() + "', Club=" + score.getClubName()
//								+ ", points=" + pointsPerCrew);
						});
					}
				}
			}
		}

		return updateScores(scores.values(), entityManager);
	}

	@Override
	public List<Score> getScores() {
		EntityManager entityManager = this.db.getEntityManager();

		return entityManager
				.createQuery("SELECT s FROM Score s WHERE s.regatta = :regatta ORDER BY s.rank ASC", Score.class)
				.setHint(JAVAX_PERSISTENCE_FETCHGRAPH, entityManager.getEntityGraph("score-club"))
				.setParameter(PARAM_REGATTA, getActiveRegatta()).getResultList();
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

	@Override
	public List<ResultEntry> getOfficialResults() {
		return getOfficialHeats().stream().map(heat -> ResultEntry.builder().heat(heat).build())
				.collect(Collectors.toList());
	}

	@Override
	public List<Heat> getHeats() {
		EntityManager entityManager = this.db.getEntityManager();

		return entityManager.createQuery("SELECT h FROM Heat h WHERE h.regatta = :regatta", Heat.class)
				.setHint(JAVAX_PERSISTENCE_FETCHGRAPH, entityManager.getEntityGraph("heat-all"))
				.setParameter(PARAM_REGATTA, getActiveRegatta()).getResultList();
	}

	private List<Heat> getOfficialHeats() {
		EntityManager entityManager = this.db.getEntityManager();

		return entityManager.createQuery("SELECT h FROM Heat h WHERE h.regatta = :regatta AND h.state = 4", Heat.class)
				.setHint(JAVAX_PERSISTENCE_FETCHGRAPH, entityManager.getEntityGraph("heat-all"))
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

	private void notifyListeners(RegattaDAO.RegattaChangedEvent event) {
		List<RegattaChangedEventListener> listeners = this.listenerManager
				.getListeners(RegattaChangedEventListener.class);
		for (RegattaChangedEventListener listener : listeners) {
			listener.regattaChanged(event);
		}
	}

	// static helpers

	private static boolean isEqualCrews(Registration reg1, Registration reg2) {
		// remove cox from comparison
		List<Crew> crews1 = reg1.getFinalCrews().stream().filter(crew -> !crew.isCox()).sorted(RegattaDAOImpl::compare)
				.collect(Collectors.toList());
		List<Crew> crews2 = reg2.getFinalCrews().stream().filter(crew -> !crew.isCox()).sorted(RegattaDAOImpl::compare)
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

	private static void findBestMatch(SetListEntry entry, Set<Registration> srcRegistrations) {
		Registration registration = entry.getRegistration();

		for (Registration srcRegistration : srcRegistrations) {
			if (registration.getClub().equals(srcRegistration.getClub())) {
				entry.setSrcRegistration(srcRegistration);
				srcRegistrations.remove(srcRegistration);
			}
		}
	}

	private static List<SetListEntry> createSetListWithEqualCrews(Map<Integer, SetListEntry> equalCrews, Race srcRace) {
		List<SetListEntry> setList = new ArrayList<>();

		List<List<HeatRegistration>> srcHeatRegsAll = getSrcHeatsByRank(srcRace);

		for (List<HeatRegistration> srcHeatRegs : srcHeatRegsAll) {
			srcHeatRegs.stream().sorted((heatReg1, heatReg2) -> {
				if (heatReg1.getFinalResult() == null || heatReg2.getFinalResult() == null) {
					return 0;
				}
				return heatReg1.getFinalResult().getNetTime().intValue() > heatReg2.getFinalResult().getNetTime()
						.intValue() ? 1 : -1;
			}).forEach(srcHeatReg -> {
				SetListEntry entry;
				// ensure the result contains a valid rank, if rank == 0 the boat did not finish
				if (srcHeatReg.getFinalResult().getRank() > 0) {
					entry = equalCrews.remove(srcHeatReg.getRegistration().getId());
					if (entry != null) {
						entry.setRank(setList.size() + 1);
						entry.setSrcHeatRregistration(srcHeatReg);
						setList.add(entry);
					}
				} else {
					entry = equalCrews.get(srcHeatReg.getRegistration().getId());
					if (entry != null) {
						entry.setSrcHeatRregistration(srcHeatReg);
					}
				}
			});
		}

		return setList;
	}

	private static List<List<HeatRegistration>> getSrcHeatsByRank(Race srcRace) {
		List<List<HeatRegistration>> srcHeatRegs = new ArrayList<>();

		for (int i = 0; i < srcRace.getRaceMode().getLaneCount(); i++) {
			srcHeatRegs.add(new ArrayList<>());
		}

		// loop over source race heats and get all heat registrations sorted by time
		srcRace.getHeats().forEach(heat -> {
			List<HeatRegistration> byRank = heat.getEntriesSortedByRank();
			for (int j = 0; j < byRank.size(); j++) {
				srcHeatRegs.get(j).add(byRank.get(j));
			}
		});
		return srcHeatRegs;
	}

}
