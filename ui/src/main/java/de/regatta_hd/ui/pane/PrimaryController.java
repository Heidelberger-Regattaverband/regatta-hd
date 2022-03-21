package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.DBConfig;
import de.regatta_hd.aquarius.DBConfigStore;
import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.ui.dialog.AboutDialog;
import de.regatta_hd.ui.dialog.DBConnectionDialog;
import de.regatta_hd.ui.util.DBTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.util.Pair;

public class PrimaryController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(PrimaryController.class.getName());

	@Inject
	private DBConfigStore dbCfgStore;
	@Inject
	@Named("version")
	private String version;

	// menu and menu items
	@FXML
	private MenuBar mainMbar;
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
	private MenuItem resultsMitm;
	@FXML
	private MenuItem heatsMitm;
	@FXML
	private ComboBox<Regatta> activeRegattaCBox;

	// fields
	private ObservableList<Regatta> regattasList = FXCollections.observableArrayList();

	// stages
	private Stage setRaceStage;
	private Stage regattasViewStage;
	private Stage racesViewStage;
	private Stage scoresViewStage;
	private Stage resultsStage;
	private Stage errorLogStage;
	private Stage heatsStage;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		updateControls(false);

		Platform.runLater(this::handleConnectOnAction);

		this.activeRegattaCBox.setItems(this.regattasList);

		this.listenerManager.addListener(RegattaDAO.RegattaChangedEventListener.class,
				event -> setTitle(event.getActiveRegatta()));

		this.listenerManager.addListener(AquariusDB.StateChangedEventListener.class, event -> {
			if (event.getAquariusDB().isOpen()) {
				this.activeRegattaCBox.setDisable(true);

				super.dbTaskRunner.run(progress -> {
					List<Regatta> regattas = super.regattaDAO.getRegattas();
					Regatta activeRegatta = super.regattaDAO.getActiveRegatta();
					return new Pair<>(regattas, activeRegatta);
				}, dbResult -> {
					try {
						this.regattasList.setAll(dbResult.getResult().getKey());
						this.activeRegattaCBox.getSelectionModel().select(dbResult.getResult().getValue());
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
						FxUtils.showErrorMessage(this.mainMbar.getScene().getWindow(), e);
					} finally {
						this.activeRegattaCBox.setDisable(false);
					}
				});
			} else {
				this.regattasList.clear();
			}
		});
	}

	private void setTitle(Regatta regatta) {
		String title = regatta != null ? regatta.getTitle() : getText("MainWindow.title");
		((Stage) getWindow()).setTitle(title);
	}

	@FXML
	void handleConnectOnAction() {
		if (!super.db.isOpen()) {
			try {
				DBConnectionDialog dialog = new DBConnectionDialog(getWindow(), super.resources,
						this.dbCfgStore.getLastSuccessful());
				Optional<DBConfig> connectionData = dialog.showAndWait();
				if (connectionData.isPresent()) {
					openDbConnection(connectionData.get());
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	private void openDbConnection(DBConfig connectionData) {
		DBTask<Pair<DBConfig, Regatta>> dbTask = super.dbTaskRunner.createTask(progress -> {
			final int MAX = 3;
			updateControls(true);

			progress.update(1, MAX, getText("login.openingDb"));
			super.db.open(connectionData);

			if (connectionData.isUpdateSchema()) {
				progress.update(2, MAX, getText("login.updatingDb"));
				super.db.updateSchema();
			}

			progress.update(3, MAX, getText("login.settingActiveRegatta"));
			Regatta activeRegatta = super.regattaDAO.getActiveRegatta();

			return new Pair<>(connectionData, activeRegatta);
		}, dbResult -> {
			try {
				Pair<DBConfig, Regatta> pair = dbResult.getResult();
				this.dbCfgStore.setLastSuccessful(pair.getKey());
				setTitle(pair.getValue());
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(this.mainMbar.getScene().getWindow(), e);
			} finally {
				updateControls(false);
			}
		}, false);

		runTaskWithProgressDialog(dbTask, getText("DatabaseConnectionDialog.title"));
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
	void handleResultsOnAction() {
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
	void handleLogRecordsOnAction() {
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

	@FXML
	void handleHeatsOnAction() {
		if (this.heatsStage == null) {
			try {
				this.heatsStage = newWindow("HeatsView.fxml", getText("heats.title"), event -> this.heatsStage = null);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			this.heatsStage.requestFocus();
		}
	}

	@FXML
	void handleAboutOnAction() {
		AboutDialog aboutDlg = new AboutDialog(getWindow(), this.resources, this.version);
		aboutDlg.showAndWait();
	}

	@FXML
	void handleActiveRegattaOnAction() {
		Regatta regatta = this.activeRegattaCBox.getSelectionModel().getSelectedItem();
		if (regatta != null) {
			super.regattaDAO.setActiveRegatta(regatta);
		}
	}

	@FXML
	private void handleExit() {
		Platform.exit();
	}

	private void updateControls(boolean isConnecting) {
		boolean isOpen = super.db.isOpen();

		if (isOpen) {
			super.dbTaskRunner.run(em -> super.regattaDAO.getActiveRegatta(), dbResult -> {
				try {
					boolean hasActiveRegatta = dbResult.getResult() != null;

					this.racesMitm.setDisable(!hasActiveRegatta);
					this.setRaceMitm.setDisable(!hasActiveRegatta);
					this.scoreMitm.setDisable(!hasActiveRegatta);
					this.resultsMitm.setDisable(!hasActiveRegatta);
					this.heatsMitm.setDisable(!hasActiveRegatta);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			});
		} else {
			this.racesMitm.setDisable(!isOpen);
			this.setRaceMitm.setDisable(!isOpen);
			this.scoreMitm.setDisable(!isOpen);
			this.resultsMitm.setDisable(!isOpen);
			this.heatsMitm.setDisable(!isOpen);
		}

		this.dbConnectMitm.setDisable(isOpen || isConnecting);
		this.dbDisconnectMitm.setDisable(!isOpen);
		this.eventsMitm.setDisable(!isOpen);
		this.errorLogMitm.setDisable(!isOpen);
	}

}
