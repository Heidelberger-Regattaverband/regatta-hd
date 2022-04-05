package de.regatta_hd.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusModule;
import de.regatta_hd.aquarius.DBLogHandler;
import de.regatta_hd.commons.fx.guice.GuiceContext;
import de.regatta_hd.commons.fx.stage.WindowManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Regatta HD JavaFX application.
 */
public class RegattaHD extends Application {

	private final GuiceContext context = new GuiceContext(this,
			() -> Arrays.asList(new UIModule(), new AquariusModule()));

	@Inject
	private DBLogHandler dbLogHandler;

	@Inject
	private WindowManager windowManager;

	@Override
	public void start(Stage primaryStage) throws IOException {
		this.context.init();

		initLogging();

		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("icon.png")) {
			primaryStage.getIcons().add(new Image(in));
		}

		ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.GERMANY);

		this.windowManager.loadStage(primaryStage,
				RegattaHD.class.getResource("/de/regatta_hd/ui/pane/PrimaryView.fxml"),
				bundle.getString("MainWindow.title"), bundle);
	}

	private void initLogging() {
		// must set before the Logger, loads logging.properties from the classpath
//		try (InputStream is = RegattaHD.class.getClassLoader().getResourceAsStream("logging.properties")) {
//			LogManager.getLogManager().readConfiguration(is);
//		}

		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.addHandler(this.dbLogHandler);
	}

	public static void main(String[] args) {
		launch();
	}
}