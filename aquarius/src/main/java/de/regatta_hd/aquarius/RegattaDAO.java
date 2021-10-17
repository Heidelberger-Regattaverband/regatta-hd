package de.regatta_hd.aquarius;

import java.io.IOException;
import java.util.List;

import de.regatta_hd.aquarius.model.AgeClass;
import de.regatta_hd.aquarius.model.BoatClass;
import de.regatta_hd.aquarius.model.Offer;
import de.regatta_hd.aquarius.model.Regatta;

/**
 * Provides access to regatta related data like offers, heats and further.
 */
public interface RegattaDAO {

	/**
	 * @return a {@link List} with all available {@link Regatta regattas} in Aquarius database.
	 */
	List<Regatta> getRegattas();

	List<Offer> getOffers();

	Offer getOffer(String raceNumber);

	List<Offer> findOffers(String raceNumberFilter, BoatClass boatClass, AgeClass ageClass, boolean lightweight);

	List<Offer> findOffers(String raceNumberFilter);

	void setRace(Offer targetOffer, Offer sourceOffer);

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
}
