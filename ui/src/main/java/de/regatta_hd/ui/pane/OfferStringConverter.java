package de.regatta_hd.ui.pane;

import java.util.HashMap;
import java.util.Map;

import de.regatta_hd.aquarius.model.Race;
import javafx.util.StringConverter;

public class OfferStringConverter extends StringConverter<Race> {

	private final Map<String, Race> offerToString = new HashMap<>();

	@Override
	public String toString(Race offer) {
		if (offer == null) {
			return null;
		}
		String toString = offer.getRaceNumber() + " - " + offer.getShortLabel() + " - " + offer.getLongLabel();
		this.offerToString.put(toString, offer);
		return toString;
	}

	@Override
	public Race fromString(String string) {
		return this.offerToString.get(string);
	}
}