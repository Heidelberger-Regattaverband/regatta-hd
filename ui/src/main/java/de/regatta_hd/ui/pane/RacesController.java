package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.ui.util.GroupModeStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

public class RacesController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(RacesController.class.getName());

	// UI Controls
	@FXML
	private Button refreshBtn;
	@FXML
	private Button setDistancesBtn;
	@FXML
	private Button setMastersAgeClassesBtn;
	@FXML
	private TableView<Race> racesTbl;
	@FXML
	private TableColumn<Race, Integer> idCol;
	@FXML
	private TableColumn<Race, Race.GroupMode> groupModeCol;

	@FXML
	private TableView<Registration> regsTbl;

	// fields
	private final ObservableList<Race> racesList = FXCollections.observableArrayList();
	private final ObservableList<Registration> regsList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		// races table
		this.racesTbl.setItems(this.racesList);
		this.racesTbl.getSortOrder().add(this.idCol);
		this.groupModeCol.setCellFactory(TextFieldTableCell.forTableColumn(new GroupModeStringConverter()));
		this.racesTbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				this.regsList.setAll(newSelection.getRegistrations());
			} else {
				this.regsList.clear();
			}
		});

		// registrations table
		this.regsTbl.setItems(this.regsList);

		loadRaces(true);
	}

	@Override
	protected void onActiveRegattaChanged(Regatta activeRegatta) {
		if (activeRegatta != null) {
			loadRaces(true);
		} else {
			this.racesList.clear();
			disableButtons(true);
		}
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("PrimaryView.racesMitm.text") + " - " + activeRegatta.getTitle()
				: getText("PrimaryView.racesMitm.text");
	}

	@FXML
	private void refresh() {
		loadRaces(true);
	}

	@FXML
	private void setDistances() {
		disableButtons(true);

		super.dbTaskRunner.runInTransaction(em -> this.regattaDAO.setDistances(), dbResult -> {
			try {
				List<Race> races = dbResult.getResult();
				if (races.isEmpty()) {
					FxUtils.showInfoDialog(getWindow(), "Keine Rennen geändert.");
				} else {
					refresh();
					FxUtils.showInfoDialog(getWindow(),
							String.format("%d Rennen geändert.", Integer.valueOf(races.size())));
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				disableButtons(false);
			}
		});
	}

	@FXML
	private void setMastersAgeClasses() {
		disableButtons(true);

		super.dbTaskRunner.runInTransaction(em -> this.regattaDAO.enableMastersAgeClasses(), dbResult -> {
			try {
				List<Race> races = dbResult.getResult();
				if (races.isEmpty()) {
					FxUtils.showInfoDialog(getWindow(), "Keine Masters Rennen geändert.");
				} else {
					refresh();
					FxUtils.showInfoDialog(getWindow(),
							String.format("%d Masters Rennen geändert.", Integer.valueOf(races.size())));
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void loadRaces(boolean refresh) {
		disableButtons(true);
		updatePlaceholder(getText("common.loadData"));
		Race selectedItem = this.racesTbl.getSelectionModel().getSelectedItem();

		super.dbTaskRunner.run(progress -> {
			if (refresh) {
				this.db.getEntityManager().clear();
			}
			return this.regattaDAO.getRaces(Race.GRAPH_CLUBS);
		}, dbResult -> {
			try {
				this.racesList.setAll(dbResult.getResult());
				this.racesTbl.getSelectionModel().select(selectedItem);
				this.racesTbl.sort();
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
		((Label) this.racesTbl.getPlaceholder()).setText(text);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.setDistancesBtn.setDisable(disabled);
		this.setMastersAgeClassesBtn.setDisable(disabled);
		this.racesTbl.setDisable(disabled);
		this.regsTbl.setDisable(disabled);
	}

}
