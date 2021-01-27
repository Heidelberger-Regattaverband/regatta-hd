package de.regatta_hd.aquarius.db;

import java.util.List;

import de.regatta_hd.aquarius.db.model.AgeClass;
import de.regatta_hd.aquarius.db.model.BoatClass;
import de.regatta_hd.aquarius.db.model.Regatta;
import de.regatta_hd.aquarius.db.model.Offer;

public interface EventDAO {

	List<Regatta> getEvents();

	Regatta getEvent(int id);

	Offer getOffer(Regatta event, String raceNumber);

	List<Offer> findOffers(Regatta event, BoatClass boatClass, AgeClass ageClass, boolean lightweight);

	List<Offer> findOffers(Regatta event, String raceNumberFilter);

	void setRace(Offer targetOffer, Offer sourceOffer);
}
