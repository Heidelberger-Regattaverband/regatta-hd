package de.regatta_hd.ui;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.ConnectionData;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class PrimaryController implements Initializable {

	@FXML
	private MenuBar menuBar;

	@FXML
	private MenuItem databaseConnect;

	@FXML
	private MenuItem databaseDisconnect;

	@Inject
	private AquariusDB aquarius;

	private ResourceBundle resources;

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
	private void handleExit() {
		Platform.exit();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resources = resources;

		updateControls();

		Platform.runLater(() -> {
			handleDatabaseConnect();
		});
	}

	private void updateControls() {
		this.databaseConnect.setDisable(this.aquarius.isOpen());
		this.databaseDisconnect.setDisable(!this.aquarius.isOpen());
	}
}
