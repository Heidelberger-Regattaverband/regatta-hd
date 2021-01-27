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
import de.regatta_hd.aquarius.db.model.Comp;
import de.regatta_hd.aquarius.db.model.CompEntries;
import de.regatta_hd.aquarius.db.model.Regatta;
import de.regatta_hd.aquarius.db.model.Offer;

@Singleton
public class EventDAOImpl extends AbstractDAOImpl implements EventDAO {

	@Override
	public List<Regatta> getEvents() {
		return getEntities(Regatta.class);
	}

	@Override
	public Regatta getEvent(int eventId) {
		return getEntity(Regatta.class, Objects.requireNonNull(eventId, "eventId is null"));
	}

	@Override
	public Offer getOffer(Regatta regatta, String raceNumber) {
		CriteriaBuilder cb = getCriteriaBuilder();

		// SELECT o FROM Offer o WHERE o.raceNumber == :nr
		CriteriaQuery<Offer> query = cb.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

		ParameterExpression<String> raceNumberParam = cb.parameter(String.class, "nr");
		ParameterExpression<Regatta> regattaParam = cb.parameter(Regatta.class, "regatta");

		query.where(cb.and( //
				cb.equal(o.get("raceNumber"), raceNumberParam), //
				cb.equal(o.get("regatta"), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(raceNumberParam.getName(), Objects.requireNonNull(raceNumber, "raceNumber is null"))
				.setParameter(regattaParam.getName(), Objects.requireNonNull(regatta, "regatta is null")) //
				.getSingleResult();
	}

	@Override
	public List<Offer> findOffers(Regatta regatta, BoatClass boatClass, AgeClass ageClass, boolean lightweight) {
		CriteriaBuilder cb = getCriteriaBuilder();

		CriteriaQuery<Offer> query = cb.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

		ParameterExpression<Regatta> regattaParam = cb.parameter(Regatta.class, "regatta");
		ParameterExpression<BoatClass> boatClassParam = cb.parameter(BoatClass.class, "boatClass");
		ParameterExpression<AgeClass> ageClassParam = cb.parameter(AgeClass.class, "ageClass");
		ParameterExpression<Boolean> lightweightParam = cb.parameter(Boolean.class, "lightweight");

		query.where(cb.and( //
				cb.equal(o.get("lightweight"), lightweightParam), //
				cb.equal(o.get("boatClass"), boatClassParam), //
				cb.equal(o.get("ageClass"), ageClassParam), //
				cb.equal(o.get("regatta"), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(lightweightParam.getName(), lightweight)
				.setParameter(boatClassParam.getName(), Objects.requireNonNull(boatClass, "boatClass is null"))
				.setParameter(ageClassParam.getName(), Objects.requireNonNull(ageClass, "ageClass is null"))
				.setParameter(regattaParam.getName(), Objects.requireNonNull(regatta, "regatta is null")) //
				.getResultList();
	}

	@Override
	public void setRace(Offer targetOffer, Offer sourceOffer) {
		List<CompEntries> targetCompEntries = new ArrayList<>();

		List<Comp> sourceComps = sourceOffer.getComps();
		List<List<CompEntries>> compEntries = new ArrayList<>();

		for (int i = 0; i < sourceComps.size(); i++) {
			Comp comp = sourceComps.get(i);

			compEntries.add(i, comp.getCompEntriesOrderedByRank());

			if (!compEntries.get(i).isEmpty()) {
				targetCompEntries.add(compEntries.get(i).get(0));
			}
		}

		Comp comp = Comp.builder().regatta(targetOffer.getRegatta()).heatNumber((short) 1).compEntries(targetCompEntries)
				.build();

		List<Comp> targetComps = new ArrayList<>();
		targetComps.add(comp);
		targetOffer.setComps(targetComps);

		persist(targetOffer);
	}

	@Override
	public List<Offer> findOffers(Regatta regatta, String raceNumber) {
		CriteriaBuilder cb = getCriteriaBuilder();

		CriteriaQuery<Offer> query = cb.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

		ParameterExpression<Regatta> regattaParam = cb.parameter(Regatta.class, "regatta");
		ParameterExpression<String> raceNumberParam = cb.parameter(String.class, "raceNumber");

		query.where(cb.and( //
				cb.like(o.get("raceNumber"), raceNumberParam), //
				cb.equal(o.get("regatta"), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(raceNumberParam.getName(), Objects.requireNonNull(raceNumber, "raceNumber is null"))
				.setParameter(regattaParam.getName(), Objects.requireNonNull(regatta, "regatta is null")) //
				.getResultList();
	}
}
