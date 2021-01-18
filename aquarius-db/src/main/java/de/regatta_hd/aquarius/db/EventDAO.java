package de.regatta_hd.aquarius.db;

import java.util.List;

import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.Offer;

public interface EventDAO {

	List<Event> getEvents();

	Event getEvent(int id);

	Offer getOffer(Event event, String raceNumber);

	List<Offer> findOffers(Event event, BoatClass boatClass, AgeClass ageClass, boolean lightweight);

	List<Offer> findOffers(Event event, String raceNumberFilter);

	void setRace(Offer targetOffer, Offer sourceOffer);
}
