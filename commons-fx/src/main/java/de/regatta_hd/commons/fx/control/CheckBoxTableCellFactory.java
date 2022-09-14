package de.regatta_hd.commons.fx.control;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

/**
 * A factory for {@link CheckBoxTableCell} instances.
 *
 * @see https://riptutorial.com/javafx/example/16421/instance-creation-in-fxml
 */
public class CheckBoxTableCellFactory {

	private CheckBoxTableCellFactory() {
		// avoid instances
	}

	public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> readonly() {
		return column -> {
			CheckBoxTableCell<S, T> checkBoxTableCell = new CheckBoxTableCell<>();
			checkBoxTableCell.setEditable(false);
			return checkBoxTableCell;
		};
	}

	public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> editable() {
		return column -> {
			CheckBoxTableCell<S, T> checkBoxTableCell = new CheckBoxTableCell<>();
			checkBoxTableCell.setEditable(true);
			return checkBoxTableCell;
		};
	}

}
