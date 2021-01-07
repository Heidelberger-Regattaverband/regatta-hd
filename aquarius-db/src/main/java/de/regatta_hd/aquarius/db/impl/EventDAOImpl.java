package de.regatta_hd.aquarius.db.impl;

import java.util.List;
import java.util.Objects;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.EventId;
import de.regatta_hd.aquarius.db.model.Offer;

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

	@Override
	public Offer getOffer(Event event, String raceNumber) {
		CriteriaBuilder cb = getCriteriaBuilder();

		// SELECT o FROM Offer o WHERE o.raceNumber == :nr
		CriteriaQuery<Offer> query = cb.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

		ParameterExpression<String> raceNumberParam = cb.parameter(String.class, "nr");
		ParameterExpression<Event> eventParam = cb.parameter(Event.class, "event");

		query.where(cb.and( //
				cb.equal(o.get("raceNumber"), raceNumberParam), //
				cb.equal(o.get("event"), eventParam) //
		));

		return createTypedQuery(query)
				.setParameter(raceNumberParam.getName(), Objects.requireNonNull(raceNumber, "raceNumber is null"))
				.setParameter(eventParam.getName(), Objects.requireNonNull(event, "event is null")).getSingleResult();
	}
}
