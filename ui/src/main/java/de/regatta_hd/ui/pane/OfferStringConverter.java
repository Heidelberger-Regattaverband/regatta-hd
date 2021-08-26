package de.regatta_hd.ui.pane;

import java.util.HashMap;
import java.util.Map;

import de.regatta_hd.aquarius.db.model.Offer;
import javafx.util.StringConverter;

public class OfferStringConverter extends StringConverter<Offer> {

	private final Map<String, Offer> offerToString = new HashMap<>();

	@Override
	public String toString(Offer offer) {
		if (offer == null) {
			return null;
		}
		String toString = offer.getRaceNumber() + " - " + offer.getShortLabel() + " - " + offer.getLongLabel();
		if (offer.isLightweight()) {
			toString += " - LW";
		}
		this.offerToString.put(toString, offer);
		return toString;
	}

	@Override
	public Offer fromString(String string) {
		return this.offerToString.get(string);
	}
}