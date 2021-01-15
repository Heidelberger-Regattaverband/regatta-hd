package de.regatta_hd.ui.pane;

import de.regatta_hd.aquarius.db.model.Offer;
import javafx.util.StringConverter;

public class OfferStringConverter extends StringConverter<Offer> {

	@Override
	public String toString(Offer offer) {
		return offer.getRaceNumber() + " - " + offer.getLongLabel();
	}

	@Override
	public Offer fromString(String string) {
		return null;
	}
}