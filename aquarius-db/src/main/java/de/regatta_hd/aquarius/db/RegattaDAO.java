package de.regatta_hd.aquarius.db;

import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Offer;
import de.regatta_hd.aquarius.db.model.Regatta;
import java.util.List;

/**
 * Provides access to regatta related data like offers, heats and further.
 */
public interface RegattaDAO {

	/**
	 * @return a {@link List} with all available {@link Regatta regattas} in
	 *         Aquarius database.
	 */
	List<Regatta> getRegattas();

	List<Offer> getOffers();

	Offer getOffer(Regatta regatta, String raceNumber);

	List<Offer> findOffers(BoatClass boatClass, AgeClass ageClass, boolean lightweight);

	List<Offer> findOffers(String raceNumberFilter);

	void setRace(Offer targetOffer, Offer sourceOffer);

	/**
	 * Sets the active regatta that is used for all DB accesses.
	 *
	 * @param regatta the active regatta
	 */
	void setActiveRegatta(Regatta regatta);

	/**
	 * Returns active regatta.
	 *
	 * @return the active {@link Regatta regatta} or <code>null</code> if not
	 *         selected.
	 */
	Regatta getActiveRegatta();
}
