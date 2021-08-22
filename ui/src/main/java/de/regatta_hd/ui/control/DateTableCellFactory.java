package de.regatta_hd.ui.control;

import java.time.Instant;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class DateTableCellFactory<S> implements Callback<TableColumn<S, Instant>, TableCell<S, Instant>> {

	@Override
	public TableCell<S, Instant> call(TableColumn<S, Instant> column) {
		return new DateTableCell<>();
	}
}
