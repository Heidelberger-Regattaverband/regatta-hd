package de.regatta_hd.aquarius.db;

import java.util.List;

import de.regatta_hd.aquarius.db.model.Event;

public interface EventDAO {

	List<Event> getEvents();
}
