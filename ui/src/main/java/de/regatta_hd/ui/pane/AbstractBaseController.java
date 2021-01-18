package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.regatta_hd.ui.FXMLLoaderFactory;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import jfxtras.scene.control.window.Window;

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

	protected void newWindow(String resource, String title, Pane parent) throws IOException {
		FXMLLoader loader = this.fxmlLoaderFactory.newLoader();
		loader.setLocation(getClass().getResource(resource));
		loader.setResources(this.resources);
		Pane pane = loader.load();

		Window window = new Window(title);
		window.getContentPane().getChildren().add(pane);
		parent.getChildren().add(window);

//		Scene scene = new Scene(parent, 800, 600);
//		Stage stage = new Stage();
//		stage.setTitle(title);
//		stage.setScene(scene);
//		stage.show();
	}

	protected String getText(String key, Object... args) {
		String text = this.resources.getString(key);
		if (args.length >= 0) {
			text = MessageFormat.format(text, args);
		}
		return text;
	}
}
