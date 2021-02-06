package de.regatta_hd.aquarius.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.google.inject.Singleton;

import de.regatta_hd.aquarius.db.RegattaDAO;
import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Heat;
import de.regatta_hd.aquarius.db.model.HeatRegistration;
import de.regatta_hd.aquarius.db.model.Offer;
import de.regatta_hd.aquarius.db.model.Regatta;

@Singleton
public class RegattaDAOImpl extends AbstractDAOImpl implements RegattaDAO {

	@Override
	public List<Regatta> getRegattas() {
		return getEntities(Regatta.class);
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
		short laneCount = targetOffer.getRaceMode().getLaneCount();

		List<HeatRegistration> targetHeatEntries = new ArrayList<>();
		List<List<HeatRegistration>> sourceCompEntries = new ArrayList<>();

		// source heats
		List<Heat> sourceHeats = sourceOffer.getHeats();
		for (int i = 0; i < sourceHeats.size(); i++) {
			Heat heat = sourceHeats.get(i);

			sourceCompEntries.add(i, heat.getHeatRegistrationsOrderedByRank());

			if (!sourceCompEntries.get(i).isEmpty()) {
				targetHeatEntries.add(sourceCompEntries.get(i).get(0));
			}
		}

		// target heats
		List<Heat> targetHeats = targetOffer.getHeats();
		for (int i = 0; i < targetHeats.size(); i++) {
			Heat heat = targetHeats.get(i);
			if (heat == null) {
				heat = Heat.builder().offer(targetOffer).regatta(targetOffer.getRegatta()).heatNumber((short) i)
						.entries(targetHeatEntries).build();
			}
		}

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
