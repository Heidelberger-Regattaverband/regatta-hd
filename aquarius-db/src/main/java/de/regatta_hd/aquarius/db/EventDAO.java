package de.regatta_hd.aquarius.db;

import java.util.List;

import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.EventId;

public interface EventDAO {

	List<Event> getEvents();
	
	Event getEvent(EventId id);
}
