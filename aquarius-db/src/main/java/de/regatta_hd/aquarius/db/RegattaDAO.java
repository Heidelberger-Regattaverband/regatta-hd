package de.regatta_hd.aquarius.db;

import java.util.List;

import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Regatta;
import de.regatta_hd.aquarius.db.model.Offer;

/**
 * Provides access to regatta related data like offers, heats and further.
 */
public interface RegattaDAO {

	/**
	 * @return a {@link List} with all available {@link Regatta regattas} in
	 *         Aquarius database.
	 */
	List<Regatta> getRegattas();

	Offer getOffer(Regatta regatta, String raceNumber);

	List<Offer> findOffers(Regatta regatta, BoatClass boatClass, AgeClass ageClass, boolean lightweight);

	List<Offer> findOffers(Regatta regatta, String raceNumberFilter);

	void setRace(Offer targetOffer, Offer sourceOffer);
}
