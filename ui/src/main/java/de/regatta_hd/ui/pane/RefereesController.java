package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.MasterDataDAO;
import de.regatta_hd.aquarius.model.Referee;
import de.regatta_hd.commons.core.ListenerManager;
import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.fx.util.FxUtils;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class RefereesController extends AbstractBaseController {
	private static final Logger logger = Logger.getLogger(RefereesController.class.getName());

	@Inject
	private MasterDataDAO masterDAO;
	@Inject
	private ListenerManager listenerManager;

	@FXML
	private Button refreshBtn;
	@FXML
	private Button deactivateAllBtn;
	@FXML
	private Button activateAllBtn;
	@FXML
	private TextField filterTxf;
	@FXML
	private TableView<Referee> refereesTbl;
	@FXML
	private TableColumn<Referee, String> idCol;
	@FXML
	private TableColumn<Referee, Boolean> activeCol;

	private final ObservableList<Referee> refereesList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.listenerManager.addListener(DBConnection.StateChangedEventListener.class, event -> {
			if (event.getDBConnection().isOpen()) {
				loadResults(true);
				disableButtons(false);
			} else {
				disableButtons(true);
				this.refereesList.clear();
			}
		});

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

		this.refereesTbl.getSortOrder().add(this.idCol);

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

		loadResults(true);
	}

	@Override
	public void shutdown() {
		// nothing to shutdown yet
	}

	@FXML
	void handleRefreshOnAction() {
		loadResults(true);
	}

	@FXML
	void handleDeactivateAllOnAction() {
		updateLicenceState(false);
	}

	@FXML
	void handleActivateAllOnAction() {
		updateLicenceState(true);
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
				FxUtils.autoResizeColumns(this.refereesTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				updatePlaceholder(getText("common.noDataAvailable"));
				disableButtons(false);
			}
		});
	}

	private void loadResults(boolean refresh) {
		disableButtons(true);
		updatePlaceholder(getText("common.loadData"));

		super.dbTaskRunner.run(progress -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.masterDAO.getReferees();
		}, dbResult -> {
			try {
				this.refereesList.setAll(dbResult.getResult());
				this.refereesTbl.sort();
				FxUtils.autoResizeColumns(this.refereesTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				updatePlaceholder(getText("common.noDataAvailable"));
				disableButtons(false);
			}
		});
	}

	private void updatePlaceholder(String text) {
		((Label) this.refereesTbl.getPlaceholder()).setText(text);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.activateAllBtn.setDisable(disabled);
		this.deactivateAllBtn.setDisable(disabled);
		this.filterTxf.setDisable(disabled);
	}

}
