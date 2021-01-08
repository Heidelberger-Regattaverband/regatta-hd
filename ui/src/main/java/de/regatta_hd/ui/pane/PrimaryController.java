package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.ConnectionData;
import de.regatta_hd.ui.dialog.DatabaseConnectionDialog;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class PrimaryController extends AbstractBaseController {

	@Inject
	private AquariusDB aquarius;

	@FXML
	private MenuItem databaseConnect;

	@FXML
	private MenuItem databaseDisconnect;

	@FXML
	private MenuItem events;

	@FXML
	private MenuItem arrangements;

	@FXML
	private MenuBar menuBar;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

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
	private void handleAboutAction(ActionEvent event) {
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
	private void handleArrangements() {
		try {
			newWindow("arrangements.fxml", "Einstellungen");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleEvents() {
		try {
			newWindow("events.fxml", "Regatten");
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
		this.arrangements.setDisable(!this.aquarius.isOpen());
	}
}
