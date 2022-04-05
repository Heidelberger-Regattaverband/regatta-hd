package de.regatta_hd.commons.fx.util;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class FxUtils {
	private static final String WINDOW_POSITION_X = "Window_Position_X";
	private static final String WINDOW_POSITION_Y = "Window_Position_Y";
	private static final String WINDOW_WIDTH = "Window_Width";
	private static final String WINDOW_HEIGHT = "Window_Height";
	private static final double DEFAULT_X = 10;
	private static final double DEFAULT_Y = 10;
	private static final double DEFAULT_WIDTH = 1024;
	private static final double DEFAULT_HEIGHT = 768;

	public static final ResourceBundle bundle = ResourceBundle.getBundle("commons-fx_messages", Locale.GERMANY);

	private FxUtils() {
		// avoid instances
	}

	public static File showSaveDialog(Window window, String fileName, String description, final String... extensions) {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter for text files
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extensions));
		fileChooser.setInitialFileName(fileName);
		// Show save file dialog
		return fileChooser.showSaveDialog(window);
	}

	public static void showErrorMessage(Window window, Throwable exception) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(window);
		alert.setTitle(bundle.getString("error.title"));
		alert.setHeaderText(exception.getClass().getCanonicalName());
		alert.setContentText(exception.getMessage());
		alert.showAndWait();
	}

	public static void showInfoDialog(Window window, String msg) {
		Alert alert = new Alert(AlertType.INFORMATION, null, ButtonType.OK);
		alert.initOwner(window);
		alert.setHeaderText(msg);
		alert.showAndWait();
	}

	public static boolean showConfirmDialog(Window window, String title, String msg) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(window);
		alert.setHeaderText(msg);
		alert.setTitle(title);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

		// Deactivate Defaultbehavior for yes-Button:
		Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
		yesButton.setText(bundle.getString("common.yes"));
		yesButton.setDefaultButton(false);

		// Activate Defaultbehavior for no-Button:
		Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
		noButton.setText(bundle.getString("common.no"));
		noButton.setDefaultButton(true);

		alert.showAndWait();
		return alert.getResult() == ButtonType.YES;
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
