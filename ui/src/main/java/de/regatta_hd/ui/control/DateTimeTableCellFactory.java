package de.regatta_hd.ui.control;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * A factory for {@link DateTimeTableCell}.
 *
 * @see https://stackoverflow.com/a/59531087
 */
public class DateTimeTableCellFactory {

	private DateTimeTableCellFactory() {
		// avoid instances
	}

	public static <S> Callback<TableColumn<S, Instant>, TableCell<S, Instant>> createShort() {
		return column -> new DateTimeTableCell<>(
				DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.GERMANY));
	}

	public static <S> Callback<TableColumn<S, Instant>, TableCell<S, Instant>> createMedium() {
		return column -> new DateTimeTableCell<>(
				DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.GERMANY));
	}

	public static <S> Callback<TableColumn<S, Instant>, TableCell<S, Instant>> createLong() {
		return column -> new DateTimeTableCell<>(
				DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withLocale(Locale.GERMANY));
	}

	public static <S> Callback<TableColumn<S, Instant>, TableCell<S, Instant>> createFull() {
		return column -> new DateTimeTableCell<>(
				DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).withLocale(Locale.GERMANY));
	}
}
