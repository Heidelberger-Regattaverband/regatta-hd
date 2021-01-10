package de.regatta_hd.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.inject.Inject;

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
public class RegattaHD extends Application {

	private RegattaHDGuiceContext context = new RegattaHDGuiceContext(this,
			() -> Arrays.asList(new GuiceModule(), new AquariusDBModule()));

	@Inject
	private FXMLLoader fxmlLoader;

	@Override
	public void start(Stage stage) throws IOException {
		this.context.init();

		ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.GERMAN);

		Scene scene = new Scene(loadFXML("/de/regatta_hd/ui/pane/primary.fxml", bundle), 640, 480);
		stage.setScene(scene);
		stage.setTitle(bundle.getString("MainWindow.title"));
		stage.show();
	}

	private Parent loadFXML(String fxml, ResourceBundle bundle) throws IOException {
		this.fxmlLoader.setLocation(RegattaHD.class.getResource(fxml));
		this.fxmlLoader.setResources(bundle);
		return this.fxmlLoader.load();
	}

	public static void main(String[] args) {
		launch();
	}

	class GuiceModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(FXMLLoaderFactory.class).toInstance(RegattaHD.this.context);
			bind(ConnectionDataStore.class).to(ConnectionDataStoreImpl.class);
		}
	}
}