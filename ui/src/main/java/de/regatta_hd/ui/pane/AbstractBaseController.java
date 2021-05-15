package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import com.google.inject.Inject;

import de.regatta_hd.ui.FXMLLoaderFactory;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

abstract class AbstractBaseController implements Initializable {

	protected URL location;

	protected ResourceBundle resources;

	@Inject
	protected FXMLLoaderFactory fxmlLoaderFactory;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.location = Objects.requireNonNull(location, "location");
		this.resources = Objects.requireNonNull(resources, "resources");
	}

	protected Stage newWindow(String resource, String title, Consumer<WindowEvent> closeHandler) throws IOException {
		FXMLLoader loader = this.fxmlLoaderFactory.newFXMLLoader();
		loader.setLocation(getClass().getResource(resource));
		loader.setResources(this.resources);
		Parent parent = loader.load();

		Stage stage = new Stage();
		stage.setTitle(title);
		stage.setScene(new Scene(parent, 800, 600));
		stage.show();
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, closeHandler::accept);

		return stage;
	}

	protected String getText(String key, Object... args) {
		String text = this.resources.getString(key);
		if (args.length > 0) {
			text = MessageFormat.format(text, args);
		}
		return text;
	}
}
