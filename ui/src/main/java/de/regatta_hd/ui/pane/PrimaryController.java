package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.DBConfiguration;
import de.regatta_hd.aquarius.db.DBConfigurationStore;
import de.regatta_hd.ui.dialog.DBConnectionDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class PrimaryController extends AbstractBaseController {

	@Inject
	private AquariusDB aquarius;

	@Inject
	private DBConfigurationStore connectionDataStore;

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
			DBConnectionDialog dialog;
			try {
				dialog = new DBConnectionDialog((Stage) this.menuBar.getScene().getWindow(), true,
						super.resources, this.connectionDataStore.getLastSuccessful());
				Optional<DBConfiguration> connectionData = dialog.showAndWait();
				if (connectionData.isPresent()) {
					Task<DBConfiguration> dbOpenTask = new Task<>() {
						@Override
						protected DBConfiguration call() throws IOException {
							PrimaryController.this.aquarius.open(connectionData.get());
							PrimaryController.this.connectionDataStore.setLastSuccessful(connectionData.get());
							updateControls();
							return connectionData.get();
						}
					};
					// start Task
					new Thread(dbOpenTask).start();
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
