package de.regatta_hd.aquarius.db;

import java.util.List;

import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.EventId;
import de.regatta_hd.aquarius.db.model.Offer;

public interface EventDAO {

	List<Event> getEvents();

	Event getEvent(EventId id);

	Offer getOffer(Event event, String raceNumber);
}
