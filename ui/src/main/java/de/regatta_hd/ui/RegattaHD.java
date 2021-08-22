package de.regatta_hd.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.db.AquariusDBModule;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Regatta HD JavaFX application.
 */
public class RegattaHD extends Application {

	private final GuiceContext context = new GuiceContext(this, () -> Arrays.asList(new AquariusDBModule()));

	@Inject
	private FXMLLoader fxmlLoader;

	@Override
	public void start(Stage primaryStage) throws IOException {
		this.context.init();

		ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.GERMAN);

		Scene scene = new Scene(loadFXML("/de/regatta_hd/ui/pane/PrimaryView.fxml", bundle), 800, 600);
		primaryStage.setScene(scene);
		primaryStage.setTitle(bundle.getString("MainWindow.title"));
		primaryStage.show();
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