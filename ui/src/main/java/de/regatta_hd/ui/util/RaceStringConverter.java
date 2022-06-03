package de.regatta_hd.ui.util;

import java.util.HashMap;
import java.util.Map;

import de.regatta_hd.aquarius.model.Race;
import javafx.util.StringConverter;

public class RaceStringConverter extends StringConverter<Race> {

	private final Map<String, Race> raceToString = new HashMap<>();

	@Override
	public String toString(Race race) {
		if (race == null) {
			return null;
		}
		String toString = String.format("%s - %s - %s - %d Anmeldungen - %d Abteilungen", race.getNumber(),
				race.getShortLabel(), race.getLongLabel(), Long.valueOf(race.getActiveRegistrations().count()),
				Long.valueOf(race.getDrivenHeats().count()));
		this.raceToString.put(toString, race);
		return toString;
	}

	@Override
	public Race fromString(String string) {
		return this.raceToString.get(string);
	}
}