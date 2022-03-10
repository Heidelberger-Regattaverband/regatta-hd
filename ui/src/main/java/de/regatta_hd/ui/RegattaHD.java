package de.regatta_hd.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDBModule;
import de.regatta_hd.aquarius.DBLogHandler;
import de.regatta_hd.ui.util.FxUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Regatta HD JavaFX application.
 */
public class RegattaHD extends Application {

	private final GuiceContext context = new GuiceContext(this, () -> Arrays.asList(new AquariusDBModule()));

	@Inject
	private FXMLLoader fxmlLoader;

	@Inject
	private DBLogHandler dbLogHandler;

	@Override
	public void start(Stage primaryStage) throws IOException {
		this.context.init();

		initLogging();

		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("icon.png")) {
			primaryStage.getIcons().add(new Image(in));
		}

		ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.GERMANY);

		FxUtils.loadSizeAndPos("Primary.fxml", primaryStage);

		Scene scene = new Scene(loadFXML("/de/regatta_hd/ui/pane/PrimaryView.fxml", bundle));
		primaryStage.setScene(scene);
		primaryStage.setTitle(bundle.getString("MainWindow.title"));

		// When the stage closes store the current size and window location.
		primaryStage.setOnCloseRequest(event -> FxUtils.storeSizeAndPos("Primary.fxml", primaryStage));

		primaryStage.show();
	}

	private void initLogging() throws IOException {
		// must set before the Logger, loads logging.properties from the classpath
		try (InputStream is = RegattaHD.class.getClassLoader().getResourceAsStream("logging.properties")) {
			LogManager.getLogManager().readConfiguration(is);
		}

		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.addHandler(this.dbLogHandler);
	}

	private Parent loadFXML(String fxml, ResourceBundle bundle) throws IOException {
		this.fxmlLoader.setLocation(RegattaHD.class.getResource(fxml));
		this.fxmlLoader.setResources(bundle);
		return this.fxmlLoader.load();
	}

	public static void main(String[] args) {
		launch();
	}
}