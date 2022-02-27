package de.regatta_hd.aquarius;

import java.io.IOException;
import java.util.EventListener;
import java.util.List;

import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Score;

/**
 * Provides access to regatta related data like offers, heats and further.
 */
public interface RegattaDAO {

	/**
	 * @return a {@link List} with all available {@link Regatta regattas} in Aquarius database.
	 */
	List<Regatta> getRegattas();

	List<Race> getRaces();

	List<Race> getRaces(String graphName);

	Race getRace(String raceNumber);

	Race getRace(String raceNumber, String graphName);

	List<ResultEntry> getOfficialResults();

	/**
	 * Sets the active regatta that is used for all DB accesses.
	 *
	 * @param regatta the active regatta
	 * @throws IOException if setting active regatta failed
	 */
	void setActiveRegatta(Regatta regatta) throws IOException;

	/**
	 * Returns active regatta.
	 *
	 * @return the active {@link Regatta regatta} or <code>null</code> if not selected.
	 */
	Regatta getActiveRegatta();

	List<Race> enableMastersAgeClasses();

	List<Race> setDistances();

	// race assignment

	List<SetListEntry> createSetList(Race race, Race srcRace);

	void setRaceHeats(Race targetRace, List<SetListEntry> setList);

	void cleanRaceHeats(Race race);

	// score

	List<Score> calculateScores();

	List<Score> getScores();

	interface RegattaChangedEvent {

		Regatta getActiveRegatta();
	}

	/**
	 * An event listener interface for regatta changed events.
	 */
	@FunctionalInterface
	interface RegattaChangedEventListener extends EventListener {

		void regattaChanged(RegattaChangedEvent event);
	}
}
