package de.regatta_hd.commons.fx.util;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.controlsfx.dialog.ProgressDialog;

import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class FxUtils {
	private static final Logger logger = Logger.getLogger(FxUtils.class.getName());

	private static final String WINDOW_POSITION_X = "Window_Position_X";
	private static final String WINDOW_POSITION_Y = "Window_Position_Y";
	private static final String WINDOW_WIDTH = "Window_Width";
	private static final String WINDOW_HEIGHT = "Window_Height";
	private static final double DEFAULT_X = 10;
	private static final double DEFAULT_Y = 10;
	private static final double DEFAULT_WIDTH = 1024;
	private static final double DEFAULT_HEIGHT = 768;

	public static final double DIALOG_WIDTH = 500;

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

	public static File showOpenDialog(Window window, String fileName, String description, final String... extensions) {
		FileChooser fileChooser = new FileChooser();

		// Set extension filter for text files
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extensions));
		fileChooser.setInitialFileName(fileName);
		// Show save file dialog
		return fileChooser.showOpenDialog(window);
	}

	public static void showErrorMessage(Window window, Throwable exception) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(window);
		alert.setTitle(bundle.getString("error.title"));
		alert.setHeaderText(exception.getClass().getCanonicalName());
		alert.setContentText(exception.getMessage());
		alert.getDialogPane().setPrefWidth(DIALOG_WIDTH);
		alert.showAndWait();
	}

	public static void showInfoDialog(Window window, String msg) {
		Alert alert = new Alert(AlertType.INFORMATION, null, ButtonType.OK);
		alert.initOwner(window);
		alert.setHeaderText(msg);
		alert.setTitle(bundle.getString("common.info"));
		alert.getDialogPane().setPrefWidth(DIALOG_WIDTH);
		alert.showAndWait();
	}

	public static boolean showConfirmDialog(Window window, String title, String msg) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(window);
		alert.setHeaderText(msg);
		alert.setTitle(title);
		alert.getDialogPane().setPrefWidth(DIALOG_WIDTH);
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

	public static <T> ProgressDialog showProgressDialog(Window window, String title, boolean cancel, Worker<T> worker) {
		ProgressDialog dialog = new ProgressDialog(worker);
		dialog.initOwner(window);
		dialog.setTitle(title);
		dialog.getDialogPane().setPrefWidth(FxUtils.DIALOG_WIDTH);
		if (cancel) {
			dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
			dialog.setOnCloseRequest(event -> {
				if (event.getEventType() == DialogEvent.DIALOG_CLOSE_REQUEST) {
					worker.cancel();
				}
			});
		}
		return dialog;
	}

	public static void loadSizeAndPos(String resource, Stage stage) {
		String node = resource.substring(resource.lastIndexOf('/') + 1);
		logger.log(Level.FINE, "Load size/pos of resource {0}", node);

		Preferences pref = Preferences.userRoot().node(node);
		stage.setX(pref.getDouble(WINDOW_POSITION_X, DEFAULT_X));
		stage.setY(pref.getDouble(WINDOW_POSITION_Y, DEFAULT_Y));
		stage.setWidth(pref.getDouble(WINDOW_WIDTH, DEFAULT_WIDTH));
		stage.setHeight(pref.getDouble(WINDOW_HEIGHT, DEFAULT_HEIGHT));
	}

	public static void storeSizeAndPos(String resource, Stage stage) {
		String node = resource.substring(resource.lastIndexOf('/') + 1);
		logger.log(Level.FINE, "Store size/pos of resource {0}", node);

		Preferences preferences = Preferences.userRoot().node(node);
		preferences.putDouble(WINDOW_POSITION_X, stage.getX());
		preferences.putDouble(WINDOW_POSITION_Y, stage.getY());
		preferences.putDouble(WINDOW_WIDTH, stage.getWidth());
		preferences.putDouble(WINDOW_HEIGHT, stage.getHeight());
	}

	public static void autoResizeColumns(TableView<?> table) {
		// Set the right policy
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		table.getColumns().stream().forEach(column -> {
			if (column.isResizable()) {
				// Minimal width = columnheader
				Text text = new Text(column.getText());
				double max = text.getLayoutBounds().getWidth();

				if (table.getItems() != null) {
					for (int i = 0; i < table.getItems().size(); i++) {
						// cell must not be empty
						if (column.getCellData(i) != null) {
							text = new Text(column.getCellData(i).toString());
							double calcwidth = text.getLayoutBounds().getWidth();
							// remember new max-width
							if (calcwidth > max) {
								max = calcwidth;
							}
						}
					}
				}
				// set the new max-widht with some extra space
				column.setPrefWidth(max + 10.0d);
			}
		});
	}

}
