package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.DBConfig;
import de.regatta_hd.aquarius.DBConfigStore;
import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.common.ListenerManager;
import de.regatta_hd.ui.dialog.DBConnectionDialog;
import de.regatta_hd.ui.util.FxUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.util.Pair;

public class PrimaryController extends AbstractBaseController {
	private static final Logger logger = Logger.getLogger(PrimaryController.class.getName());

	@Inject
	private DBConfigStore dbCfgStore;

	@Inject
	private ListenerManager listenerManager;

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

		updateControls(false);

		Platform.runLater(this::handleDatabaseConnect);

		this.listenerManager.addListener(RegattaDAO.RegattaChangedEventListener.class, event -> {
			setTitle(event.getActiveRegatta());
		});
	}

	private void setTitle(Regatta regatta) {
		Stage stage = (Stage) this.mainMbar.getScene().getWindow();
		String title = regatta != null ? regatta.getTitle() : getText("MainWindow.title");
		stage.setTitle(title);
	}

	@FXML
	private void handleDatabaseConnect() {
		if (!super.db.isOpen()) {
			Stage stage = (Stage) this.mainMbar.getScene().getWindow();
			try {
				DBConnectionDialog dialog = new DBConnectionDialog(stage, true, super.resources,
						this.dbCfgStore.getLastSuccessful());
				Optional<DBConfig> connectionData = dialog.showAndWait();
				if (connectionData.isPresent()) {
					openDbConnection(connectionData);
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	private void openDbConnection(Optional<DBConfig> connectionData) {
		super.dbTask.run(() -> {
			updateControls(true);
			super.db.open(connectionData.get());
			Regatta activeRegatta = super.regattaDAO.getActiveRegatta();
			return new Pair<>(connectionData.get(), activeRegatta);
		}, dbResult -> {
			try {
				Pair<DBConfig, Regatta> pair = dbResult.getResult();
				this.dbCfgStore.setLastSuccessful(pair.getKey());
				setTitle(pair.getValue());
			} catch (Exception e) {
				FxUtils.showErrorMessage(e);
			} finally {
				updateControls(false);
			}
		});
	}

	@FXML
	private void handleDatabaseDisconnect() {
		super.db.close();
		final Stage stage = (Stage) this.mainMbar.getScene().getWindow();
		stage.setTitle(getText("MainWindow.title"));
		updateControls(false);
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

	private void updateControls(boolean isConnecting) {
		boolean isOpen = super.db.isOpen();

		if (isOpen) {
			this.dbTask.run(super.regattaDAO::getActiveRegatta, dbResult -> {
				try {
					boolean hasActiveRegatta = dbResult.getResult() != null;

					this.offersMitm.setDisable(!hasActiveRegatta);
					this.divisionsMitm.setDisable(!hasActiveRegatta);
					this.scoreMitm.setDisable(!hasActiveRegatta);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			});
		} else {
			this.offersMitm.setDisable(!isOpen);
			this.divisionsMitm.setDisable(!isOpen);
			this.scoreMitm.setDisable(!isOpen);
		}

		this.dbConnectMitm.setDisable(isOpen || isConnecting);
		this.dbDisconnectMitm.setDisable(!isOpen);
		this.eventsMitm.setDisable(!isOpen);
	}
}
