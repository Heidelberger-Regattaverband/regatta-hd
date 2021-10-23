package de.regatta_hd.ui.pane;

import de.regatta_hd.aquarius.model.Race;
import javafx.util.StringConverter;

public class GroupModeStringConverter extends StringConverter<Race.GroupMode> {

	@Override
	public String toString(Race.GroupMode groupMode) {
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
	public Race.GroupMode fromString(String string) {
		return null;
	}
}