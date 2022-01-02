package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.DBConfig;
import de.regatta_hd.aquarius.DBConfigStore;
import de.regatta_hd.ui.dialog.DBConnectionDialog;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class PrimaryController extends AbstractBaseController {
	private static final Logger logger = Logger.getLogger(PrimaryController.class.getName());

	@Inject
	private AquariusDB aquariusDb;

	@Inject
	private DBConfigStore dbCfgStore;

	@FXML
	private MenuItem dbConnectMitm;

	@FXML
	private MenuItem dbDisconnectMitm;
	@FXML
	private MenuItem eventsMitm;
	@FXML
	private MenuItem offersMitm;
	@FXML
	private MenuItem divisionsMitm;
	@FXML
	private MenuItem scoreMitm;

	@FXML
	private MenuBar mainMbar;

	private Stage setRaceStage;

	private Stage eventViewStage;

	private Stage offersViewStage;

	private Stage scoresViewStage;

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
				dialog = new DBConnectionDialog((Stage) this.mainMbar.getScene().getWindow(), true, super.resources,
						this.dbCfgStore.getLastSuccessful());
				Optional<DBConfig> connectionData = dialog.showAndWait();
				if (connectionData.isPresent()) {
					this.dbTask.run(() -> {
						PrimaryController.this.aquariusDb.open(connectionData.get());
						PrimaryController.this.dbCfgStore.setLastSuccessful(connectionData.get());
						updateControls();
					});
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
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
				this.setRaceStage = newWindow("SetRaceView.fxml", getText("PrimaryView.setRaceMitm.text"),
						event -> this.setRaceStage = null);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			this.setRaceStage.requestFocus();
		}
	}

	@FXML
	private void handleEvents() {
		if (this.eventViewStage == null) {
			try {
				this.eventViewStage = newWindow("RegattasView.fxml", getText("PrimaryView.regattasMitm.text"),
						event -> this.eventViewStage = null);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			this.eventViewStage.requestFocus();
		}
	}

	@FXML
	private void handleOffers() {
		if (this.offersViewStage == null) {
			try {
				this.offersViewStage = newWindow("OffersView.fxml", getText("PrimaryView.racesMitm.text"),
						event -> this.offersViewStage = null);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			this.offersViewStage.requestFocus();
		}
	}

	@FXML
	private void handleScore() {
		if (this.scoresViewStage == null) {
			try {
				this.scoresViewStage = newWindow("ScoresView.fxml", getText("PrimaryView.scoresMitm.text"),
						event -> this.scoresViewStage = null);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			this.scoresViewStage.requestFocus();
		}
	}

	@FXML
	private void handleExit() {
		Platform.exit();
	}

	private void updateControls() {
		boolean isOpen = this.aquariusDb.isOpen();

		this.dbConnectMitm.setDisable(isOpen);
		this.dbDisconnectMitm.setDisable(!isOpen);
		this.eventsMitm.setDisable(!isOpen);
		this.offersMitm.setDisable(!isOpen);
		this.divisionsMitm.setDisable(!isOpen);
		this.scoreMitm.setDisable(!isOpen);
	}
}
