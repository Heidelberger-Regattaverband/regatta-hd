package de.regatta_hd.aquarius.db.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.aquarius.db.RegattaDAO;
import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Heat;
import de.regatta_hd.aquarius.db.model.HeatRegistration;
import de.regatta_hd.aquarius.db.model.Offer;
import de.regatta_hd.aquarius.db.model.Regatta;
import de.regatta_hd.common.ConfigService;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Root;

@Singleton
public class RegattaDAOImpl extends AbstractDAOImpl implements RegattaDAO {

	private static final String ACTIVE_REGATTA = "activeRegatta";

	private Regatta activeRegatta;

	@Inject
	private ConfigService cfgService;

	@Override
	public List<Regatta> getRegattas() {
		return getEntities(Regatta.class);
	}

	@Override
	public Offer getOffer(String raceNumber) {
		var critBuilder = getCriteriaBuilder();

		// SELECT o FROM Offer o WHERE o.raceNumber == :nr
		CriteriaQuery<Offer> query = critBuilder.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

		ParameterExpression<String> raceNumberParam = critBuilder.parameter(String.class, "nr");
		ParameterExpression<Regatta> regattaParam = critBuilder.parameter(Regatta.class, "regatta");

		query.where(critBuilder.and( //
				critBuilder.equal(o.get("raceNumber"), raceNumberParam), //
				critBuilder.equal(o.get("regatta"), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(raceNumberParam.getName(), requireNonNull(raceNumber, "raceNumber is null"))
				.setParameter(regattaParam.getName(), requireNonNull(getActiveRegatta(), "activeRegatta is null")) //
				.getSingleResult();
	}

	@Override
	public List<Offer> findOffers(BoatClass boatClass, AgeClass ageClass, boolean lightweight) {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Offer> query = critBuilder.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

		ParameterExpression<Regatta> regattaParam = critBuilder.parameter(Regatta.class, "regatta");
		ParameterExpression<BoatClass> boatClassParam = critBuilder.parameter(BoatClass.class, "boatClass");
		ParameterExpression<AgeClass> ageClassParam = critBuilder.parameter(AgeClass.class, "ageClass");
		ParameterExpression<Boolean> lightweightParam = critBuilder.parameter(Boolean.class, "lightweight");

		query.where(critBuilder.and( //
				critBuilder.equal(o.get("lightweight"), lightweightParam), //
				critBuilder.equal(o.get("boatClass"), boatClassParam), //
				critBuilder.equal(o.get("ageClass"), ageClassParam), //
				critBuilder.equal(o.get("regatta"), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(lightweightParam.getName(), lightweight)
				.setParameter(boatClassParam.getName(), requireNonNull(boatClass, "boatClass is null"))
				.setParameter(ageClassParam.getName(), requireNonNull(ageClass, "ageClass is null"))
				.setParameter(regattaParam.getName(), requireNonNull(getActiveRegatta(), "activeRegatta is null")) //
				.getResultList();
	}

	@Override
	public void setRace(Offer targetOffer, Offer sourceOffer) {
		List<HeatRegistration> targetHeatEntries = new ArrayList<>();
		List<List<HeatRegistration>> sourceHeatRegistrations = new ArrayList<>();

		// source heats
		List<Heat> sourceHeats = sourceOffer.getHeats();
		for (int i = 0; i < sourceHeats.size(); i++) {
			Heat heat = sourceHeats.get(i);

			sourceHeatRegistrations.add(i, heat.getHeatRegistrationsOrderedByRank());

			if (!sourceHeatRegistrations.get(i).isEmpty()) {
				targetHeatEntries.add(sourceHeatRegistrations.get(i).get(0));
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
	public List<Offer> findOffers(String raceNumber) {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Offer> query = critBuilder.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

		ParameterExpression<Regatta> regattaParam = critBuilder.parameter(Regatta.class, "regatta");
		ParameterExpression<String> raceNumberParam = critBuilder.parameter(String.class, "raceNumber");

		query.where(critBuilder.and( //
				critBuilder.like(o.get("raceNumber"), raceNumberParam), //
				critBuilder.equal(o.get("regatta"), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(raceNumberParam.getName(), requireNonNull(raceNumber, "raceNumber is null"))
				.setParameter(regattaParam.getName(), requireNonNull(getActiveRegatta(), "activeRegatta is null")) //
				.getResultList();
	}

	@Override
	public List<Offer> getOffers() {
		var critBuilder = getCriteriaBuilder();

		CriteriaQuery<Offer> query = critBuilder.createQuery(Offer.class);
		Root<Offer> o = query.from(Offer.class);

		ParameterExpression<Regatta> regattaParam = critBuilder.parameter(Regatta.class, "regatta");

		query.where(critBuilder.and( //
				critBuilder.equal(o.get("regatta"), regattaParam) //
		));

		return createTypedQuery(query) //
				.setParameter(regattaParam.getName(), requireNonNull(getActiveRegatta(), "activeRegatta is null")) //
				.getResultList();
	}

	@Override
	public void setActiveRegatta(Regatta regatta) {
		this.activeRegatta = regatta;

		try {
			if (this.activeRegatta != null) {
				this.cfgService.setProperty(ACTIVE_REGATTA, Integer.toString(this.activeRegatta.getId()));
			} else {
				this.cfgService.removeProperty(ACTIVE_REGATTA);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Regatta getActiveRegatta() {
		if (this.activeRegatta == null) {
			try {
				String regattaId = this.cfgService.getProperty(ACTIVE_REGATTA);
				if (StringUtils.isNotBlank(regattaId)) {
					this.activeRegatta = getEntity(Regatta.class, Integer.parseInt(regattaId));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.activeRegatta;
	}
}
