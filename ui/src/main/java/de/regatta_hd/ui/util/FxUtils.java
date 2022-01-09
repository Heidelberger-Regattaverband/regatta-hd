package de.regatta_hd.ui.util;

import java.util.prefs.Preferences;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FxUtils {
	private static final String WINDOW_POSITION_X = "Window_Position_X";
	private static final String WINDOW_POSITION_Y = "Window_Position_Y";
	private static final String WINDOW_WIDTH = "Window_Width";
	private static final String WINDOW_HEIGHT = "Window_Height";
	private static final double DEFAULT_X = 10;
	private static final double DEFAULT_Y = 10;
	private static final double DEFAULT_WIDTH = 1024;
	private static final double DEFAULT_HEIGHT = 768;

	private FxUtils() {
		// avoid instances
	}

	public static void showErrorMessage(Throwable exception) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Ein Programmfehler ist aufgetreten");
		alert.setHeaderText(exception.getClass().getCanonicalName());
		alert.setContentText(exception.getMessage());
		alert.showAndWait();
	}

	public static void showInfoDialog(String msg) {
		Alert alert = new Alert(AlertType.INFORMATION, null, ButtonType.OK);
		alert.setHeaderText(msg);
		alert.showAndWait();
	}

	public static void loadSizeAndPos(String resource, Stage stage) {
		Preferences pref = Preferences.userRoot().node(resource);
		stage.setX(pref.getDouble(WINDOW_POSITION_X, DEFAULT_X));
		stage.setY(pref.getDouble(WINDOW_POSITION_Y, DEFAULT_Y));
		stage.setWidth(pref.getDouble(WINDOW_WIDTH, DEFAULT_WIDTH));
		stage.setHeight(pref.getDouble(WINDOW_HEIGHT, DEFAULT_HEIGHT));
	}

	public static void storeSizeAndPos(String resource, Stage stage) {
		Preferences preferences = Preferences.userRoot().node(resource);
		preferences.putDouble(WINDOW_POSITION_X, stage.getX());
		preferences.putDouble(WINDOW_POSITION_Y, stage.getY());
		preferences.putDouble(WINDOW_WIDTH, stage.getWidth());
		preferences.putDouble(WINDOW_HEIGHT, stage.getHeight());
	}

	public static void autoResizeColumns(TableView<?> table) {
		// Set the right policy
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		table.getColumns().stream().forEach(column -> {
			// Minimal width = columnheader
			Text t = new Text(column.getText());
			double max = t.getLayoutBounds().getWidth();

			if (table.getItems() != null) {
				for (int i = 0; i < table.getItems().size(); i++) {
					// cell must not be empty
					if (column.getCellData(i) != null) {
						t = new Text(column.getCellData(i).toString());
						double calcwidth = t.getLayoutBounds().getWidth();
						// remember new max-width
						if (calcwidth > max) {
							max = calcwidth;
						}
					}
				}
			}
			// set the new max-widht with some extra space
			column.setPrefWidth(max + 10.0d);
		});
	}

}
