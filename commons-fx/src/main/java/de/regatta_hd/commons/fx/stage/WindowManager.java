package de.regatta_hd.commons.fx.stage;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.stage.Stage;
import javafx.stage.Window;

public interface WindowManager {

	/**
	 * Loads the given FXML resource into the primary stage.
	 *
	 * @param primaryStage    the primary stage
	 * @param fxmlResourceUrl URL of FXML resource
	 * @param title           the window title
	 * @param bundle          the resource bundle
	 */
	void loadPrimaryStage(Stage primaryStage, URL fxmlResourceUrl, String title, ResourceBundle bundle);

	/**
	 * Creates a new stage and loads given FXML resource into it.
	 *
	 * @param fxmlResourceUrl URL of FXML resource
	 * @param title           the window title
	 * @param bundle          the resource bundle
	 * @param styles          optional styles
	 * @return the newly created {@link Stage}
	 */
	Stage newStage(URL fxmlResourceUrl, String title, ResourceBundle bundle, String... styles);

	/**
	 * Creates a new stage and loads given FXML resource into it.
	 *
	 * @param fxmlResourceUrl URL of FXML resource
	 * @param title           the window title
	 * @param bundle          the resource bundle
	 * @param styles          optional styles
	 * @return the newly created {@link Stage}
	 */
	Stage newStage(URL fxmlResourceUrl, String title, ResourceBundle bundle, Window owner, String... styles);
}
