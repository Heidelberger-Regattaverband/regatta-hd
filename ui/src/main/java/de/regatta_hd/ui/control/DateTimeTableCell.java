package de.regatta_hd.ui.control;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;

public class DateTimeTableCell<B> extends TableCell<B, Date> {

	private DatePicker datePicker;

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
	public void updateItem(Date item, boolean empty) {
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
				setText(getDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
				setGraphic(null);
			}
		}
	}

	private void createDatePicker() {
		this.datePicker = new DatePicker(getDate());
		this.datePicker.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		this.datePicker.setOnAction(e -> {
			System.out.println("Committed: " + this.datePicker.getValue().toString());
			commitEdit(Date.from(this.datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
		});
//        datePicker.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//            if (!newValue) {
//                commitEdit(Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
//            }
//        });
	}

	private LocalDate getDate() {
		return getItem() == null ? LocalDate.now() : getItem().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
}