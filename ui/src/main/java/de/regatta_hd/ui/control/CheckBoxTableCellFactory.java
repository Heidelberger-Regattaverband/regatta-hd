package de.regatta_hd.ui.control;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

public class CheckBoxTableCellFactory {

	private CheckBoxTableCellFactory() {
		// avoid instances
	}

	public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> create() {
		return column -> new CheckBoxTableCell<>();
	}
}
