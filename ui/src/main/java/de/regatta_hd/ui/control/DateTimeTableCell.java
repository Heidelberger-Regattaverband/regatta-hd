package de.regatta_hd.ui.control;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import javafx.scene.control.TableCell;

public class DateTimeTableCell<B> extends TableCell<B, Instant> { // NOSONAR

	@Override
	public void updateItem(Instant item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setText(null);
		} else {
			LocalDateTime localDateTime = getItem().atZone(ZoneId.systemDefault()).toLocalDateTime();
			String text = localDateTime
					.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.GERMANY));
			setText(text);
		}
	}

}