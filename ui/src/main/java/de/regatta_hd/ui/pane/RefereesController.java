package de.regatta_hd.ui.pane;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.MasterDataDAO;
import de.regatta_hd.aquarius.model.Referee;
import de.regatta_hd.commons.core.ListenerManager;
import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.db.DBConnection.StateChangedEventListener;
import de.regatta_hd.commons.fx.db.DBTask;
import de.regatta_hd.commons.fx.stage.PaneController;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.ui.UIModule;

public class RefereesController extends PaneController {
	private static final Logger logger = Logger.getLogger(RefereesController.class.getName());

	@Inject
	private MasterDataDAO masterDAO;
	@Inject
	private ListenerManager listenerManager;

	// toolbar
	@FXML
	private Button refreshBtn;
	@FXML
	private Button deactivateAllBtn;
	@FXML
	private Button activateAllBtn;
	@FXML
	private TextField filterTxf;
	@FXML
	private Button importBtn;

	// referees table
	@FXML
	private TableView<Referee> refereesTbl;
	@FXML
	private TableColumn<Referee, String> idCol;
	@FXML
	private TableColumn<Referee, Boolean> activeCol;
	@FXML
	private TableColumn<Referee, String> lastNameCol;

	// injections
	@Inject
	@Named(UIModule.CONFIG_SHOW_ID_COLUMN)
	private BooleanProperty showIdColumn;

	// fields
	private final ObservableList<Referee> refereesList = FXCollections.observableArrayList();

	private final StateChangedEventListener stateChangedEventListener = event -> {
		if (event.getDBConnection().isOpen()) {
			loadReferees(true);
			disableButtons(false);
		} else {
			disableButtons(true);
			this.refereesList.clear();
		}
	};

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.listenerManager.addListener(DBConnection.StateChangedEventListener.class, this.stateChangedEventListener);

		this.idCol.visibleProperty().bind(this.showIdColumn);
		this.activeCol.setCellValueFactory(cellData -> {
			BooleanProperty property = cellData.getValue().activeProperty();
			property.addListener((observable, newValue, oldValue) -> {
				super.dbTaskRunner.runInTransaction(progress -> {
					return super.db.getEntityManager().merge(cellData.getValue());
				}, dbResult -> {
					try {
						dbResult.getResult();
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
						FxUtils.showErrorMessage(getWindow(), e);
					}
				});
			});
			return property;
		});

		this.refereesTbl.getSortOrder().add(this.lastNameCol);

		// table sorting and filtering: https://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/

		// 1. Wrap the ObservableList in a FilteredList (initially display all data).
		FilteredList<Referee> filteredData = new FilteredList<>(this.refereesList, r -> true);

		// 2. Set the filter Predicate whenever the filter changes.
		this.filterTxf.textProperty()
				.addListener((observable, oldValue, newValue) -> filteredData.setPredicate(referee -> {
					// If filter text is empty, display all persons.
					if (newValue == null || newValue.isEmpty()) {
						return true;
					}

					// Compare first name and last name of every person with filter text.
					String lowerCaseFilter = newValue.toLowerCase();

					return referee.getFirstName().toLowerCase().contains(lowerCaseFilter)
							|| referee.getLastName().toLowerCase().contains(lowerCaseFilter)
							|| referee.getCity().toLowerCase().contains(lowerCaseFilter);
				}));

		// 3. Wrap the FilteredList in a SortedList.
		SortedList<Referee> sortedData = new SortedList<>(filteredData);

		// 4. Bind the SortedList comparator to the TableView comparator.
		sortedData.comparatorProperty().bind(this.refereesTbl.comparatorProperty());

		// 5. Add sorted (and filtered) data to the table.
		this.refereesTbl.setItems(sortedData);

		loadReferees(false);
	}

	@Override
	public void shutdown() {
		this.listenerManager.removeListener(StateChangedEventListener.class, this.stateChangedEventListener);
	}

	@FXML
	void handleRefreshOnAction() {
		loadReferees(true);
	}

	@FXML
	void handleDeactivateAllOnAction() {
		updateLicenceState(false);
	}

	@FXML
	void handleActivateAllOnAction() {
		updateLicenceState(true);
	}

	@FXML
	void handleImportOnAction() {
		disableButtons(true);

		File importFile = FxUtils.showOpenDialog(getWindow(), null, "Wettkampfrichter XML Datei", "*.xml");

		if (importFile != null) {
			DBTask<Integer> dbTask = super.dbTaskRunner.createTask(progress -> {
				try (InputStream reader = new BufferedInputStream(Files.newInputStream(importFile.toPath()))) {
					int count = this.masterDAO.importReferees(reader, progress);
					return Integer.valueOf(count);
				}
			}, dbResult -> {
				try {
					Integer count = dbResult.getResult();
					FxUtils.showInfoDialog(getWindow(), getText("referees.import.succeeded", count));
					loadReferees(true);
				} catch (CancellationException e) {
					logger.log(Level.FINEST, e.getMessage(), e);
					FxUtils.showInfoDialog(getWindow(), getText("referees.import.canceled"));
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					disableButtons(false);
				}
			}, true);

			runTaskWithProgressDialog(dbTask, getText("referees.import.title"), true);
		} else {
			disableButtons(false);
		}
	}

	private void updateLicenceState(boolean licenceState) {
		disableButtons(true);

		super.dbTaskRunner.runInTransaction(progress -> {
			this.masterDAO.updateAllRefereesLicenceState(licenceState);
			super.db.getEntityManager().clear();
			return this.masterDAO.getReferees();
		}, dbResult -> {
			try {
				this.refereesList.setAll(dbResult.getResult());
				this.refereesTbl.sort();
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void loadReferees(boolean refresh) {
		disableButtons(true);
		Referee selectedItem = this.refereesTbl.getSelectionModel().getSelectedItem();

		super.dbTaskRunner.run(progress -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.masterDAO.getReferees();
		}, dbResult -> {
			try {
				this.refereesList.setAll(dbResult.getResult());
				this.refereesTbl.sort();
				this.refereesTbl.getSelectionModel().select(selectedItem);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.activateAllBtn.setDisable(disabled);
		this.deactivateAllBtn.setDisable(disabled);
		this.filterTxf.setDisable(disabled);
		this.importBtn.setDisable(disabled);
		this.refereesTbl.setDisable(disabled);
	}

}
