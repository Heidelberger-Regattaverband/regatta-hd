package de.regatta_hd.aquarius.db.impl;

import java.util.List;
import java.util.Objects;

import javax.inject.Singleton;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.EventId;

@Singleton
public class EventDAOImpl extends AbstractDAOImpl implements EventDAO {

	@Override
	public List<Event> getEvents() {
		return getEntities(Event.class);
	}

	@Override
	public Event getEvent(EventId eventId) {
		return getEntity(Event.class, Objects.requireNonNull(eventId, "eventId is null"));
	}
}
