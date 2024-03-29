package de.regatta_hd.commons.fx.control;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * A factory for {@link DateTableCell} instances.
 *
 * @see https://riptutorial.com/javafx/example/16421/instance-creation-in-fxml
 */
public class DateTableCellFactory {

	private DateTableCellFactory() {
		// avoid instances
	}

	public static <S> Callback<TableColumn<S, Instant>, TableCell<S, Instant>> createMedium() {
		return column -> new DateTableCell<>(
				DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.GERMANY));
	}
}
