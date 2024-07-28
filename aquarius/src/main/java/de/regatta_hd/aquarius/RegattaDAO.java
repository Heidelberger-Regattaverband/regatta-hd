package de.regatta_hd.aquarius;

import java.util.EventListener;
import java.util.List;

import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Regatta;

/**
 * Provides access to regatta related data like offers, heats and further.
 */
public interface RegattaDAO {

	// active regatta

	/**
	 * Sets the active regatta that is used for all DB accesses.
	 *
	 * @param regatta the active regatta
	 */
	void setActiveRegatta(Regatta regatta);

	/**
	 * Returns active regatta.
	 *
	 * @return the active {@link Regatta regatta} or <code>null</code> if not selected.
	 */
	Regatta getActiveRegatta();

	/**
	 * @return a {@link List} with all available {@link Regatta regattas} in Aquarius database.
	 */
	List<Regatta> getRegattas();

	// races
	List<Race> getRaces();

	List<Race> getRaces(String graphName);

	Race getRace(String raceNumber, String graphName);

	List<Heat> getHeats(String graphName);

	List<Race> enableMastersAgeClasses();

	List<Race> setDistances();

	// seeding list and setting race
	List<SeedingListEntry> createSeedingList(Race race, Race srcRace);

	void setRaceHeats(Race race, List<SeedingListEntry> seedingList);

	void cleanRaceHeats(Race race);

	Heat swapResults(HeatRegistration source, HeatRegistration target);

	// regatta changed event and listener

	/**
	 * This event signals a changed active regatta.
	 */
	interface ActiveRegattaChangedEvent {

		Regatta getActiveRegatta();
	}

	/**
	 * An event listener interface for regatta changed events.
	 */
	@FunctionalInterface
	interface RegattaChangedEventListener extends EventListener {

		void regattaChanged(ActiveRegattaChangedEvent event);
	}
}
