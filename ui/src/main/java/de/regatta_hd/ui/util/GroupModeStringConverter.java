package de.regatta_hd.ui.util;

import javafx.util.StringConverter;

import de.regatta_hd.aquarius.model.Race;

public class GroupModeStringConverter extends StringConverter<Race.GroupMode> {

	@Override
	public String toString(Race.GroupMode groupMode) {
		if (groupMode == null) {
			return null;
		}
		return switch (groupMode) {
		case NONE -> "-";
		case AGE -> "AK";
		case PERFORMANCE -> "LG";
		case PERFORMANCE_AGE -> "LG/AK";
		default -> null;
		};
	}

	@Override
	public Race.GroupMode fromString(String string) {
		return null;
	}
}