package de.regatta_hd.ui.pane;

import de.regatta_hd.aquarius.model.Offer;
import javafx.util.StringConverter;

public class GroupModeStringConverter extends StringConverter<Offer.GroupMode> {

	@Override
	public String toString(Offer.GroupMode groupMode) {
		if (groupMode == null) {
			return null;
		}
		switch (groupMode) {
		case NONE:
			return "-";
		case AGE:
			return "AK";
		case PERFORMANCE:
			return "LG";
		case PERFORMANCE_AGE:
			return "LG/AK";
		default:
			return null;
		}
	}

	@Override
	public Offer.GroupMode fromString(String string) {
		return null;
	}
}