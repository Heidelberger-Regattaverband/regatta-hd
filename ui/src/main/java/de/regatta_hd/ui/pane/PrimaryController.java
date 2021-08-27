package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.DBConfig;
import de.regatta_hd.aquarius.DBConfigStore;
import de.regatta_hd.ui.dialog.DBConnectionDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class PrimaryController extends AbstractBaseController {

	@Inject
	private AquariusDB aquariusDb;

	@Inject
	private DBConfigStore dbCfgStore;

	@FXML
	private MenuItem databaseConnect;

	@FXML
	private MenuItem databaseDisconnect;

	@FXML
	private MenuItem eventsMitm;

	@FXML
	private MenuItem offersMitm;

	@FXML
	private MenuItem divisionsMitm;

	@FXML
	private MenuBar menuBar;

	private Stage setRaceStage;

	private Stage eventViewStage;

	private Stage offersViewStage;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		updateControls();

		Platform.runLater(this::handleDatabaseConnect);
	}

	@FXML
	private void handleDatabaseConnect() {
		if (!this.aquariusDb.isOpen()) {
			DBConnectionDialog dialog;
			try {
				dialog = new DBConnectionDialog((Stage) this.menuBar.getScene().getWindow(), true, super.resources,
						this.dbCfgStore.getLastSuccessful());
				Optional<DBConfig> connectionData = dialog.showAndWait();
				if (connectionData.isPresent()) {
					Task<DBConfig> dbOpenTask = new Task<>() {
						@Override
						protected DBConfig call() throws IOException {
							PrimaryController.this.aquariusDb.open(connectionData.get());
							PrimaryController.this.dbCfgStore.setLastSuccessful(connectionData.get());
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
		this.aquariusDb.close();
		updateControls();
	}

	@FXML
	private void handleSetRace() {
		if (this.setRaceStage == null) {
			try {
				this.setRaceStage = newWindow("SetRaceView.fxml", getText("PrimaryView.MenuItem.SetRace.text"),
						event -> this.setRaceStage = null);
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
				this.eventViewStage = newWindow("RegattasView.fxml", getText("PrimaryView.MenuItem.Events.text"),
						event -> this.eventViewStage = null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.eventViewStage.requestFocus();
		}
	}

	@FXML
	private void handleOffers() {
		if (this.offersViewStage == null) {
			try {
				this.offersViewStage = newWindow("OffersView.fxml", getText("PrimaryView.MenuItem.Offers.text"),
						event -> this.offersViewStage = null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.offersViewStage.requestFocus();
		}
	}

	@FXML
	private void handleExit() {
		Platform.exit();
	}

	private void updateControls() {
		this.databaseConnect.setDisable(this.aquariusDb.isOpen());
		this.databaseDisconnect.setDisable(!this.aquariusDb.isOpen());
		this.eventsMitm.setDisable(!this.aquariusDb.isOpen());
		this.offersMitm.setDisable(!this.aquariusDb.isOpen());
		this.divisionsMitm.setDisable(!this.aquariusDb.isOpen());
	}
}
