package de.regatta_hd.aquarius.db.impl;

import java.util.List;

import javax.inject.Singleton;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.Event;

@Singleton
public class EventDAOImpl extends AbstractDAOImpl implements EventDAO {

	@Override
	public List<Event> getEvents() {
		return getEntities(Event.class);
	}
}
