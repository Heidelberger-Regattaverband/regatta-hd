package de.regatta_hd.aquarius.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.CompEntries;
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

		return createTypedQuery(query) //
				.setParameter(raceNumberParam.getName(), Objects.requireNonNull(raceNumber, "raceNumber is null"))
				.setParameter(eventParam.getName(), Objects.requireNonNull(event, "event is null")) //
				.getSingleResult();
	}

	@Override
	public List<Offer> findOffers(Event event, BoatClass boatClass, AgeClass ageClass, boolean lightweight) {
		CriteriaBuilder cb = getCriteriaBuilder();

		CriteriaQuery<Offer> query = cb.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

		ParameterExpression<Event> eventParam = cb.parameter(Event.class, "event");
		ParameterExpression<BoatClass> boatClassParam = cb.parameter(BoatClass.class, "boatClass");
		ParameterExpression<AgeClass> ageClassParam = cb.parameter(AgeClass.class, "ageClass");
		ParameterExpression<Boolean> lightweightParam = cb.parameter(Boolean.class, "lightweight");

		query.where(cb.and( //
				cb.equal(o.get("lightweight"), lightweightParam), //
				cb.equal(o.get("boatClass"), boatClassParam), //
				cb.equal(o.get("ageClass"), ageClassParam), //
				cb.equal(o.get("event"), eventParam) //
		));

		return createTypedQuery(query) //
				.setParameter(lightweightParam.getName(), lightweight)
				.setParameter(boatClassParam.getName(), Objects.requireNonNull(boatClass, "boatClass is null"))
				.setParameter(ageClassParam.getName(), Objects.requireNonNull(ageClass, "ageClass is null"))
				.setParameter(eventParam.getName(), Objects.requireNonNull(event, "event is null")) //
				.getResultList();
	}

	@Override
	public void setRace(Offer targetOffer, Offer sourceOffer) {
		List<List<CompEntries>> raceEntries = new ArrayList<>();

		sourceOffer.getComps().forEach(comps -> {
			comps.getCompEntries().forEach(compEntry -> {
				compEntry.getEntry().getCrews();
			});
		});
	}
}
