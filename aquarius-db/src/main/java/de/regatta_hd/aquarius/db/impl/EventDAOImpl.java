package de.regatta_hd.aquarius.db.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.Event;

@Singleton
public class EventDAOImpl implements EventDAO {

	@Inject
	private AquariusDB aquariusDb;

	@Override
	public List<Event> getEvents() {
		CriteriaQuery<Event> query = this.aquariusDb.getCriteriaBuilder().createQuery(Event.class);
		Root<Event> from = query.from(Event.class);
		query.select(from);

		return this.aquariusDb.getEntityManager().createQuery(query).getResultList();
	}
}
