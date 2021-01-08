package de.regatta_hd.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

abstract class AbstractBaseController implements Initializable {

	protected URL location;

	protected ResourceBundle resources;

	@Inject
	protected FXMLLoader fxmlLoader;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.location = Objects.requireNonNull(location, "location");
		this.resources = Objects.requireNonNull(resources, "resources");
	}

	protected void newWindow(String resource, String title) throws IOException {
		this.fxmlLoader.setLocation(App.class.getResource(resource));
		this.fxmlLoader.setResources(this.resources);
		Parent parent = this.fxmlLoader.load();

		Scene scene = new Scene(parent, 600, 400);
		Stage stage = new Stage();
		stage.setTitle(title);
		stage.setScene(scene);
		stage.show();
	}
}
