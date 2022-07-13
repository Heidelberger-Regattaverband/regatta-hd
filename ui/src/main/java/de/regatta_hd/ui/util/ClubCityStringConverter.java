package de.regatta_hd.ui.util;

import de.regatta_hd.aquarius.model.Club;
import javafx.util.StringConverter;

public class ClubCityStringConverter extends StringConverter<Club> {

	@Override
	public String toString(Club club) {
		if (club == null) {
			return null;
		}
		return club.getCity();
	}

	@Override
	public Club fromString(String string) {
		return null;
	}

}