package de.regatta_hd.ui.control;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;

public class DateTableCell<B> extends TableCell<B, Instant> { // NOSONAR
	private static final Logger logger = Logger.getLogger(DateTableCell.class.getName());

	private DatePicker datePicker;

	private final DateTimeFormatter formatter;

	public DateTableCell(DateTimeFormatter formatter) {
		this.formatter = Objects.requireNonNull(formatter, "formatter must not be null");
	}

	@Override
	public void startEdit() {
		if (!isEmpty()) {
			super.startEdit();
			createDatePicker();
			setText(null);
			setGraphic(this.datePicker);
		}
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getDate().toString());
		setGraphic(null);
	}

	@Override
	public void updateItem(Instant item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (this.datePicker != null) {
					this.datePicker.setValue(getDate());
				}
				setText(null);
				setGraphic(this.datePicker);
			} else {
				setText(getDate().format(this.formatter));
				setGraphic(null);
			}
		}
	}

	private void createDatePicker() {
		this.datePicker = new DatePicker(getDate());
		this.datePicker.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		this.datePicker.setOnAction(e -> {
			logger.log(Level.FINE, "Committed: {0}", this.datePicker.getValue());
			commitEdit(this.datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
		});
//        datePicker.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//            if (!newValue) {
//                commitEdit(Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
//            }
//        });
	}

	private LocalDate getDate() {
		return getItem() == null ? LocalDate.now() : getItem().atZone(ZoneId.systemDefault()).toLocalDate();
	}
}