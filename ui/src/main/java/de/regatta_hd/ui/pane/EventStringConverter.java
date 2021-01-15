package de.regatta_hd.ui.pane;

import de.regatta_hd.aquarius.db.model.Event;
import javafx.util.StringConverter;

public class EventStringConverter extends StringConverter<Event> {

	@Override
	public String toString(Event event) {
		if (event == null) {
			return null;
		}
		return event.getTitle();
	}

	@Override
	public Event fromString(String string) {
		return null;
	}
}