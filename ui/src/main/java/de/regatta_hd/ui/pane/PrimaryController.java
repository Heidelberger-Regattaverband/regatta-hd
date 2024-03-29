package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.util.Pair;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.MasterDataDAO;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.db.DBConfig;
import de.regatta_hd.commons.db.DBConfigStore;
import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.fx.db.DBTask;
import de.regatta_hd.commons.fx.dialog.AboutDialog;
import de.regatta_hd.commons.fx.dialog.DBConnectionDialog;
import de.regatta_hd.commons.fx.stage.WindowManager;
import de.regatta_hd.commons.fx.util.FxUtils;

public class PrimaryController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(PrimaryController.class.getName());

	// injections
	@Inject
	private WindowManager windowManager;
	@Inject
	private DBConfigStore dbCfgStore;
	@Inject
	@Named("version")
	private String version;
	@Inject
	private MasterDataDAO masterData;

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
	private MenuItem refereesMitm;
	@FXML
	private MenuItem racesMitm;
	@FXML
	private MenuItem setRaceMitm;
	@FXML
	private MenuItem errorLogMitm;
	@FXML
	private MenuItem heatsMitm;
	@FXML
	private ComboBox<Regatta> activeRegattaCBox;
	@FXML
	private Label dbNameLbl;

	// fields
	private ObservableList<Regatta> regattasList = FXCollections.observableArrayList();

	private Stage openStage(String resource, String title) {
		String styleLocation = this.getClass().getResource("/style.css").toExternalForm();
		return this.windowManager.newStage(getClass().getResource(resource), title, super.bundle, styleLocation);
	}

	private final DBConnection.StateChangedEventListener dbStateChangedEventListener = event -> {
		if (event.getDBConnection().isOpen()) {
			this.activeRegattaCBox.setDisable(true);

			super.dbTaskRunner.run(progress -> {
				List<Regatta> regattas = super.regattaDAO.getRegattas();
				Regatta activeRegatta = super.regattaDAO.getActiveRegatta();
				return new Pair<>(regattas, activeRegatta);
			}, dbResult -> {
				this.dbNameLbl.setText(event.getDBConnection().getName());

				try {
					this.regattasList.setAll(dbResult.getResult().getKey());
					this.activeRegattaCBox.getSelectionModel().select(dbResult.getResult().getValue());
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					this.activeRegattaCBox.setDisable(false);
				}
			});
		} else {
			this.regattasList.clear();
			this.dbNameLbl.setText("");
		}
	};

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		updateControls(false);

		this.activeRegattaCBox.setItems(this.regattasList);

		this.listenerManager.addListener(DBConnection.StateChangedEventListener.class,
				this.dbStateChangedEventListener);

		Platform.runLater(this::handleConnectOnAction);
	}

	@Override
	protected void onActiveRegattaChanged(Regatta activeRegatta) {
		// nothing to do
	}

	@Override
	public void shutdown() {
		super.listenerManager.removeListener(DBConnection.StateChangedEventListener.class,
				this.dbStateChangedEventListener);

		super.shutdown();
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("MainWindow.title") + " - " + activeRegatta.getTitle()
				: getText("MainWindow.title");
	}

	// fxml event handlers

	@FXML
	void handleConnectOnAction() {
		if (!super.db.isOpen()) {
			try {
				DBConnectionDialog dialog = new DBConnectionDialog(getWindow(), this.dbCfgStore.getLastSuccessful());
				Optional<DBConfig> connectionData = dialog.showAndWait();
				if (connectionData.isPresent()) {
					openDbConnection(connectionData.get());
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	@FXML
	void handleDatabaseDisconnect() {
		super.db.close();
		final Stage stage = (Stage) this.mainMbar.getScene().getWindow();
		stage.setTitle(getText("MainWindow.title"));
		updateControls(false);
	}

	@FXML
	void handleSetRaceOnAction() {
		openStage("SetRaceView.fxml", getText("PrimaryView.setRaceMitm.text"));
	}

	@FXML
	void handleEvents() {
		openStage("RegattasView.fxml", getText("PrimaryView.regattasMitm.text"));
	}

	@FXML
	void handleRefereesOnAction() {
		openStage("RefereesView.fxml", getText("common.referees"));
	}

	@FXML
	void handleConfigOnAction() {
		openStage("ConfigView.fxml", getText("common.config"));
	}

	@FXML
	void handleRacesOnAction() {
		openStage("RacesView.fxml", getText("PrimaryView.racesMitm.text"));
	}

	@FXML
	void handleLogRecordsOnAction() {
		openStage("ErrorLogView.fxml", getText("PrimaryView.errorLogMitm.text"));
	}

	@FXML
	void handleHeatsOnAction() {
		openStage("HeatsView.fxml", getText("heats.title"));
	}

	@FXML
	void handleAboutOnAction() {
		String aquariusVersion = this.masterData.getAquariusVersion();
		if (aquariusVersion == null) {
			aquariusVersion = getText("about.unknown");
		}
		AboutDialog aboutDlg = new AboutDialog(getWindow(), this.bundle.getString("about.title"),
				this.bundle.getString("about.header"),
				MessageFormat.format(this.bundle.getString("about.text"), this.version, aquariusVersion));
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
	void handleExit() {
		Platform.exit();
	}

	// private

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
			} catch (CancellationException e) {
				logger.log(Level.FINEST, e.getMessage(), e);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				updateControls(false);
			}
		}, false);

		runTaskWithProgressDialog(dbTask, getText("DatabaseConnectionDialog.title"), false);
	}

	private void updateControls(boolean isConnecting) {
		boolean isOpen = super.db.isOpen();

		if (isOpen) {
			super.dbTaskRunner.run(em -> super.regattaDAO.getActiveRegatta(), dbResult -> {
				try {
					boolean hasActiveRegatta = dbResult.getResult() != null;

					this.racesMitm.setDisable(!hasActiveRegatta);
					this.setRaceMitm.setDisable(!hasActiveRegatta);
					this.heatsMitm.setDisable(!hasActiveRegatta);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			});
		} else {
			this.racesMitm.setDisable(!isOpen);
			this.setRaceMitm.setDisable(!isOpen);
			this.heatsMitm.setDisable(!isOpen);
		}

		this.dbConnectMitm.setDisable(isOpen || isConnecting);
		this.dbDisconnectMitm.setDisable(!isOpen);
		this.eventsMitm.setDisable(!isOpen);
		this.refereesMitm.setDisable(!isOpen);
		this.errorLogMitm.setDisable(!isOpen);
	}

}
