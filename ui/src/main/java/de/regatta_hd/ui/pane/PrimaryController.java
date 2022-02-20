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

public class PrimaryController extends AbstractRegattaController {
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
	private MenuItem racesMitm;
	@FXML
	private MenuItem setRaceMitm;
	@FXML
	private MenuItem scoreMitm;
	@FXML
	private MenuItem errorLogMitm;

	@FXML
	private MenuBar mainMbar;

	// stages
	private Stage setRaceStage;
	private Stage regattasViewStage;
	private Stage racesViewStage;
	private Stage scoresViewStage;
	private Stage resultsStage;
	private Stage errorLogStage;

	@FXML
	private MenuItem resultsMitm;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		updateControls(false);

		Platform.runLater(this::handleDatabaseConnect);

		this.listenerManager.addListener(RegattaDAO.RegattaChangedEventListener.class,
				event -> setTitle(event.getActiveRegatta()));
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
				logger.log(Level.SEVERE, e.getMessage(), e);
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
	private void handleSetRaceOnAction() {
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
		if (this.regattasViewStage == null) {
			try {
				this.regattasViewStage = newWindow("RegattasView.fxml", getText("PrimaryView.regattasMitm.text"),
						event -> this.regattasViewStage = null);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			this.regattasViewStage.requestFocus();
		}
	}

	@FXML
	private void handleRacesOnAction() {
		if (this.racesViewStage == null) {
			try {
				this.racesViewStage = newWindow("OffersView.fxml", getText("PrimaryView.racesMitm.text"),
						event -> this.racesViewStage = null);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			this.racesViewStage.requestFocus();
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

					this.racesMitm.setDisable(!hasActiveRegatta);
					this.setRaceMitm.setDisable(!hasActiveRegatta);
					this.scoreMitm.setDisable(!hasActiveRegatta);
					this.resultsMitm.setDisable(!hasActiveRegatta);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			});
		} else {
			this.racesMitm.setDisable(!isOpen);
			this.setRaceMitm.setDisable(!isOpen);
			this.scoreMitm.setDisable(!isOpen);
			this.resultsMitm.setDisable(!isOpen);
		}

		this.dbConnectMitm.setDisable(isOpen || isConnecting);
		this.dbDisconnectMitm.setDisable(!isOpen);
		this.eventsMitm.setDisable(!isOpen);
		this.errorLogMitm.setDisable(!isOpen);
	}

	@FXML
	public void handleResultsOnAction() {
		if (this.resultsStage == null) {
			try {
				this.resultsStage = newWindow("ResultsView.fxml", getText("common.results"),
						event -> this.resultsStage = null);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			this.resultsStage.requestFocus();
		}
	}

	@FXML
	public void handleLogRecordsOnAction() {
		if (this.errorLogStage == null) {
			try {
				this.errorLogStage = newWindow("ErrorLogView.fxml", getText("PrimaryView.errorLogMitm.text"),
						event -> this.errorLogStage = null);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			this.errorLogStage.requestFocus();
		}
	}
}
