package de.regatta_hd.ui.util;

import javafx.util.StringConverter;

import de.regatta_hd.aquarius.model.Regatta;

public class RegattaStringConverter extends StringConverter<Regatta> {

	@Override
	public String toString(Regatta event) {
		if (event == null) {
			return null;
		}
		return event.getTitle();
	}

	@Override
	public Regatta fromString(String string) {
		return null;
	}
}