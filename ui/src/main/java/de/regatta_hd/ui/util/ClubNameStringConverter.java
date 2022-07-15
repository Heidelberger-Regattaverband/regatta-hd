package de.regatta_hd.ui.util;

import de.regatta_hd.aquarius.model.Club;
import javafx.util.StringConverter;

public class ClubNameStringConverter extends StringConverter<Club> {

	@Override
	public String toString(Club club) {
		if (club == null) {
			return null;
		}
		return club.getName();
	}

	@Override
	public Club fromString(String string) {
		return null;
	}

}