package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.ConnectionData;
import de.regatta_hd.aquarius.db.ConnectionDataStore;
import de.regatta_hd.ui.dialog.DatabaseConnectionDialog;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class PrimaryController extends AbstractBaseController {

	@Inject
	private AquariusDB aquarius;

	@Inject
	private ConnectionDataStore connectionDataStore;

	@FXML
	private MenuItem databaseConnect;

	@FXML
	private MenuItem databaseDisconnect;

	@FXML
	private MenuItem eventsMitm;

	@FXML
	private MenuItem divisionsMitm;

	@FXML
	private MenuBar menuBar;

	private Stage setRaceStage;

	private Stage eventViewStage;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		updateControls();

		Platform.runLater(() -> {
			handleDatabaseConnect();
		});
	}

	@FXML
	private void handleDatabaseConnect() {
		if (!this.aquarius.isOpen()) {
			DatabaseConnectionDialog dialog;
			try {
				dialog = new DatabaseConnectionDialog((Stage) this.menuBar.getScene().getWindow(), true,
						super.resources, this.connectionDataStore.getLastSuccessful());
				Optional<ConnectionData> result = dialog.showAndWait();
				if (result.isPresent()) {
					this.aquarius.open(result.get());
					this.connectionDataStore.setLastSuccessful(result.get());
				}
			} catch (IOException e) {
				e.printStackTrace();
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
	private void handleSetRace() {
		if (this.setRaceStage == null) {
			try {
				this.setRaceStage = newWindow("SetRaceView.fxml", getText("PrimaryView.MenuItem.SetRace.text"),
						(event) -> {
							this.setRaceStage = null;
						});
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.setRaceStage.requestFocus();
		}
	}

	@FXML
	private void handleEvents() {
		if (this.eventViewStage == null) {
			try {
				this.eventViewStage = newWindow("EventsView.fxml", getText("PrimaryView.MenuItem.Events.text"),
						(event) -> {
							this.setRaceStage = null;
						});
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.eventViewStage.requestFocus();
		}
	}

	@FXML
	private void handleExit() {
		Platform.exit();
	}

	private void updateControls() {
		this.databaseConnect.setDisable(this.aquarius.isOpen());
		this.databaseDisconnect.setDisable(!this.aquarius.isOpen());
		this.eventsMitm.setDisable(!this.aquarius.isOpen());
		this.divisionsMitm.setDisable(!this.aquarius.isOpen());
	}
}
