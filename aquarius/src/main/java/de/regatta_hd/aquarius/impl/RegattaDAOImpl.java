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
import de.regatta_hd.aquarius.SeedingListEntry;
import de.regatta_hd.aquarius.model.Club;
import de.regatta_hd.aquarius.model.Crew;
import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Race.GroupMode;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.Result;
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
	public void setRaceHeats(Race race, List<SeedingListEntry> setList) {
		short laneCount = race.getRaceMode().getLaneCount();

		LinkedList<SeedingListEntry> stack = new LinkedList<>(setList);
		final EntityManager entityManager = super.db.getEntityManager();

		// get all planed heats ordered by the heat number
		race.getDrivenHeats().forEach(heat -> {
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

		notifyListeners(new ActiveRegattaChangedEventImpl(this, this.activeRegatta));
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
	public List<SeedingListEntry> createSeedingList(Race race, Race srcRace) {
		Map<Integer, SeedingListEntry> diffCrews = new HashMap<>();

		Set<Registration> srcRegistrations = ConcurrentHashMap.newKeySet();
		srcRegistrations.addAll(srcRace.getActiveRegistrations().collect(Collectors.toSet()));

		Map<Integer, SeedingListEntry> equalCrews = new HashMap<>();

		// remove cancelled registrations
		race.getActiveRegistrations().forEach(registration -> {
			SeedingListEntry entry = SeedingListEntry.builder().registration(registration).equalCrew(false).build();
			diffCrews.put(entry.getId(), entry);

			for (Registration srcRegistration : srcRegistrations) {

				// look for equal crew in the source registration
				if (isEqualCrews(srcRegistration, registration)) {
					SeedingListEntry equalCrewEntry = diffCrews.remove(registration.getId());
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

		List<SeedingListEntry> seedingList = createSetListWithEqualCrews(equalCrews, srcRace);

		// add not added registrations with equal crews, e.g. boat did not finish
		equalCrews.values().forEach(entry -> diffCrews.put(entry.getId(), entry));

		// sort the remaining registrations according their bib
		diffCrews.values().stream().sorted((reg1, reg2) -> reg1.getBib() > reg2.getBib() ? 1 : -1).forEach(entry -> {
			entry.setRank(seedingList.size() + 1);
			findBestMatch(entry, srcRegistrations);
			seedingList.add(entry);
		});

		return seedingList;
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
						if (raceIsSet && heatReg.getHeat().getDivisionNumber() == 1) {
							pointsBoat = Integer.valueOf(pointsBoat.intValue() * 2);
						}

						float pointsPerCrew = (float) pointsBoat.intValue() / (float) numRowers;

						// ignore cox of boat
						heatReg.getRegistration().getCrews().stream().filter(crew -> !crew.isCox()).forEach(crew -> {
							Score score = scores.computeIfAbsent(crew.getAthlet().getClub(),
									key -> Score.builder().clubId(key.getId()).regattaId(regatta.getId()).club(key)
											.regatta(regatta).points(0.0f).build());
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

	private List<Score> updateScores(Collection<Score> scores, EntityManager entityManager) {
		entityManager.createQuery("DELETE FROM Score s WHERE s.regatta = :regatta")
				.setParameter(PARAM_REGATTA, getActiveRegatta()).executeUpdate();
		entityManager.clear();

		List<Score> scoresResult = scores.stream().sorted((score1, score2) -> {
			if (score1.getPoints() == score2.getPoints()) {
				return 0;
			}
			return score1.getPoints() > score2.getPoints() ? -1 : 1;
		}).collect(Collectors.toList());

		for (int i = 0; i < scoresResult.size(); i++) {
			Score score = scoresResult.get(i);
			score.setRank((short) (i + 1));
			score.getClubName(); // get club in current session
			entityManager.persist(score);
		}

		entityManager.flush();

		return scoresResult;
	}

	@Override
	public List<Score> getScores() {
		EntityManager entityManager = this.db.getEntityManager();

		return entityManager
				.createQuery("SELECT s FROM Score s WHERE s.regatta = :regatta ORDER BY s.rank ASC", Score.class)
				.setHint(JAVAX_PERSISTENCE_FETCHGRAPH, entityManager.getEntityGraph(Score.GRAPH_ALL))
				.setParameter(PARAM_REGATTA, getActiveRegatta()).getResultList();
	}

	@Override
	public List<Race> enableMastersAgeClasses() {
		List<Race> races = new ArrayList<>();

		EntityManager entityManager = this.db.getEntityManager();

		getRaces().forEach(race -> {
			if (race.getAgeClass().isMasters() && !race.getGroupMode().equals(GroupMode.AGE)) {
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
	public List<Heat> getHeats(String graphName) {
		EntityManager entityManager = this.db.getEntityManager();

		TypedQuery<Heat> query = entityManager
				.createQuery("SELECT h FROM Heat h WHERE h.regatta = :regatta", Heat.class)
				.setParameter(PARAM_REGATTA, getActiveRegatta());
		if (graphName != null) {
			EntityGraph<?> entityGraph = this.db.getEntityManager().getEntityGraph(graphName);
			query.setHint(JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
		}

		return query.getResultList();
	}

	private List<Heat> getOfficialHeats() {
		EntityManager entityManager = this.db.getEntityManager();

		return entityManager.createQuery("SELECT h FROM Heat h WHERE h.regatta = :regatta AND h.state = 4", Heat.class)
				.setHint(JAVAX_PERSISTENCE_FETCHGRAPH, entityManager.getEntityGraph("heat-all"))
				.setParameter(PARAM_REGATTA, getActiveRegatta()).getResultList();
	}

	@Override
	public Heat swapResults(HeatRegistration source, HeatRegistration target) {
		Result result1 = source.getFinalResult();
		Result result2 = target.getFinalResult();

		String comment = result2.getComment();
		Integer dayTime = result2.getDayTime();
		Integer delta = result2.getDelta();
		String displayType = result2.getDisplayType();
		String displayValue = result2.getDisplayValue();
		Integer netTime = result2.getNetTime();
		String params = result2.getParams();
		byte rank = result2.getRank();
		String resultType = result2.getResultType();
		Integer sortValue = result2.getSortValue();
		byte splitNr = result2.getSplitNr();

		result2.setComment(result1.getComment());
		result2.setDayTime(result1.getDayTime());
		result2.setDelta(result1.getDelta());
		result2.setDisplayType(result1.getDisplayType());
		result2.setDisplayValue(result1.getDisplayValue());
		result2.setNetTime(result1.getNetTime());
		result2.setParams(result1.getParams());
		result2.setRank(result1.getRank());
		result2.setResultType(result1.getResultType());
		result2.setSortValue(result1.getSortValue());
		result2.setSplitNr(result1.getSplitNr());

		result1.setComment(comment);
		result1.setDayTime(dayTime);
		result1.setDelta(delta);
		result1.setDisplayType(displayType);
		result1.setDisplayValue(displayValue);
		result1.setNetTime(netTime);
		result1.setParams(params);
		result1.setRank(rank);
		result1.setResultType(resultType);
		result1.setSortValue(sortValue);
		result1.setSplitNr(splitNr);

		EntityManager entityManager = this.db.getEntityManager();
		entityManager.merge(result1);
		entityManager.merge(result2);
		entityManager.flush();

		return source.getHeat();
	}

	private void notifyListeners(RegattaDAO.ActiveRegattaChangedEvent event) {
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

	private static void findBestMatch(SeedingListEntry entry, Set<Registration> srcRegistrations) {
		Registration registration = entry.getRegistration();

		for (Registration srcRegistration : srcRegistrations) {
			if (registration.getClub().equals(srcRegistration.getClub())) {
				entry.setSrcRegistration(srcRegistration);
				srcRegistrations.remove(srcRegistration);
			}
		}
	}

	private static List<SeedingListEntry> createSetListWithEqualCrews(Map<Integer, SeedingListEntry> equalCrews,
			Race srcRace) {
		List<SeedingListEntry> setList = new ArrayList<>();

		List<List<HeatRegistration>> srcHeatRegsAll = getSrcHeatsByRank(srcRace);

		for (List<HeatRegistration> srcHeatRegs : srcHeatRegsAll) {
			srcHeatRegs.stream().sorted((heatReg1, heatReg2) -> {
				if (heatReg1.getFinalResult() == null || heatReg2.getFinalResult() == null) {
					return 0;
				}
				return heatReg1.getFinalResult().getNetTime().intValue() > heatReg2.getFinalResult().getNetTime()
						.intValue() ? 1 : -1;
			}).forEach(srcHeatReg -> {
				SeedingListEntry entry;
				// ensure the result contains a valid rank, if rank == 0 the boat did not finish
				if (srcHeatReg.getFinalResult().getRank() > 0) {
					entry = equalCrews.remove(srcHeatReg.getRegistration().getId());
					if (entry != null) {
						entry.setRank(setList.size() + 1);
						entry.setSrcHeatRegistration(srcHeatReg);
						setList.add(entry);
					}
				} else {
					entry = equalCrews.get(srcHeatReg.getRegistration().getId());
					if (entry != null) {
						entry.setSrcHeatRegistration(srcHeatReg);
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
		srcRace.getDrivenHeats().forEach(heat -> {
			List<HeatRegistration> byRank = heat.getEntriesSortedByRank();
			for (int j = 0; j < byRank.size(); j++) {
				srcHeatRegs.get(j).add(byRank.get(j));
			}
		});
		return srcHeatRegs;
	}

}
