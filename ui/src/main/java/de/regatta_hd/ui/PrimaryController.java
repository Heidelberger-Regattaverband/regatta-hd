package de.regatta_hd.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.ConnectionData;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class PrimaryController implements Initializable {

	@Inject
	private AquariusDB aquarius;

	@FXML
	private MenuItem databaseConnect;

	@FXML
	private MenuItem databaseDisconnect;

	@FXML
	private MenuItem events;

	@Inject
	private FXMLLoader fxmlLoader;

	@FXML
	private MenuBar menuBar;

	private ResourceBundle resources;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resources = Objects.requireNonNull(resources, "resources");

		updateControls();

		Platform.runLater(() -> {
			handleDatabaseConnect();
		});
	}

	/**
	 * Handle action related to "About" menu item.
	 *
	 * @param event Event on "About" menu item.
	 */
	@FXML
	private void handleAboutAction(final ActionEvent event) {
		System.out.println("You clicked on About!");
	}

	@FXML
	private void handleDatabaseConnect() {
		if (!this.aquarius.isOpen()) {
			DatabaseConnectionDialog dialog = new DatabaseConnectionDialog((Stage) this.menuBar.getScene().getWindow(),
					true, this.resources);
			Optional<ConnectionData> result = dialog.showAndWait();
			if (result.isPresent()) {
				this.aquarius.open(result.get());
			}
		}
		updateControls();
	}

	@FXML
	private void handleDatabaseDisconnect() {
		this.aquarius.close();
		updateControls();
	}

	@FXML
	private void handleEvents() {
		try {
			this.fxmlLoader.setLocation(App.class.getResource("events.fxml"));
			this.fxmlLoader.setResources(this.resources);
			/*
			 * if "fx:controller" is not set in fxml
			 * fxmlLoader.setController(NewWindowController);
			 */
			Scene scene = new Scene(this.fxmlLoader.load(), 600, 400);
			Stage stage = new Stage();
			stage.setTitle("Regatten");
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleExit() {
		Platform.exit();
	}

	private void updateControls() {
		this.databaseConnect.setDisable(this.aquarius.isOpen());
		this.databaseDisconnect.setDisable(!this.aquarius.isOpen());
		this.events.setDisable(!this.aquarius.isOpen());
	}
}
