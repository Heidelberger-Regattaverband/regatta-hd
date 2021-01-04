package de.regatta_hd.ui;

import java.io.IOException;
import java.util.Arrays;

import javax.inject.Inject;

import com.gluonhq.ignite.guice.GuiceContext;
import com.google.inject.AbstractModule;

import de.regatta_hd.aquarius.db.AquariusDBModule;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

	private GuiceContext context = new GuiceContext(this,
			() -> Arrays.asList(new GuiceModule(), new AquariusDBModule()));

	@Inject
	private FXMLLoader fxmlLoader;

	@Override
	public void start(Stage stage) throws IOException {
		this.context.init();

		Scene scene = new Scene(loadFXML("primary"), 640, 480);
		stage.setScene(scene);
		stage.show();
	}

	private Parent loadFXML(String fxml) throws IOException {
		this.fxmlLoader.setLocation(App.class.getResource(fxml + ".fxml"));
		return this.fxmlLoader.load();
	}

	public static void main(String[] args) {
		launch();
	}

	class GuiceModule extends AbstractModule {
		@Override
		protected void configure() {
		}
	}
}