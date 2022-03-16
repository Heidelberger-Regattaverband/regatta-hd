package de.regatta_hd.ui.control;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javafx.scene.control.TableCell;

public class DateTimeTableCell<B> extends TableCell<B, Instant> { // NOSONAR

	private final DateTimeFormatter formatter;

	public DateTimeTableCell(DateTimeFormatter formatter) {
		this.formatter = Objects.requireNonNull(formatter, "formatter must not be null");
	}

	@Override
	public void updateItem(Instant item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setText(null);
		} else {
			Instant instant = getItem();
			if (instant != null) {
				ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
				setText(dateTime.format(this.formatter));
			}
		}
	}

}