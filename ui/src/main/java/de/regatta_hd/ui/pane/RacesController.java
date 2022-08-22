package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.table.TableFilter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.commons.fx.util.FxConstants;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.schemas.xml.XMLDataLoader;
import de.regatta_hd.ui.UIModule;
import de.regatta_hd.ui.util.GroupModeStringConverter;
import de.rudern.schemas.service.meldungen._2010.RegattaMeldungen;
import de.rudern.schemas.service.meldungen._2010.TMeldung;
import de.rudern.schemas.service.meldungen._2010.TRennen;
import jakarta.xml.bind.JAXBException;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

public class RacesController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(RacesController.class.getName());

	// toolbar
	@FXML
	private Button refreshBtn;
	@FXML
	private Button setDistancesBtn;
	@FXML
	private Button setMastersAgeClassesBtn;
	@FXML
	private Button importAltRegsBtn;

	// races table
	@FXML
	private TableView<Race> racesTbl;
	@FXML
	private TableColumn<Race, Integer> idCol;
	@FXML
	private TableColumn<Race, Race.GroupMode> groupModeCol;
	@FXML
	private TableColumn<Race, Integer> regsIdCol;

	// registrations table
	@FXML
	private TableView<Registration> regsTbl;
	@FXML
	private TableColumn<Registration, Short> regsBibCol;

	@Inject
	@Named(UIModule.CONFIG_SHOW_ID_COLUMN)
	private BooleanProperty showIdColumn;

	// fields
	private final ObservableList<Race> racesList = FXCollections.observableArrayList();
	private final ObservableList<Registration> regsList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.idCol.visibleProperty().bind(this.showIdColumn);
		this.regsIdCol.visibleProperty().bind(this.showIdColumn);

		// races table
		this.racesTbl.setItems(this.racesList);
		this.racesTbl.getSortOrder().add(this.idCol);

		this.groupModeCol.setCellFactory(TextFieldTableCell.forTableColumn(new GroupModeStringConverter()));
		this.racesTbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				this.regsList.setAll(newSelection.getRegistrations());
				this.regsTbl.sort();
			} else {
				this.regsList.clear();
			}
		});
		this.racesTbl.setRowFactory(row -> new TableRow<>() {
			@Override
			public void updateSelected(boolean selected) {
				super.updateSelected(selected);
				if (selected && getItem().isCancelled()) {
					pseudoClassStateChanged(FxConstants.PC_HIGHLIGHTED_SELECTED, true);
					pseudoClassStateChanged(FxConstants.PC_HIGHLIGHTED, false);
				} else {
					pseudoClassStateChanged(FxConstants.PC_HIGHLIGHTED_SELECTED, false);
				}
			}

			@Override
			public void updateItem(Race item, boolean empty) {
				super.updateItem(item, empty);
				pseudoClassStateChanged(FxConstants.PC_HIGHLIGHTED, item != null && item.isCancelled());
			}
		});

		// registrations table
		this.regsTbl.setItems(this.regsList);
		this.regsTbl.getSortOrder().add(this.regsBibCol);
		this.regsTbl.setRowFactory(row -> new TableRow<>() {
			@Override
			public void updateItem(Registration item, boolean empty) {
				super.updateItem(item, empty);
				pseudoClassStateChanged(FxConstants.PC_HIGHLIGHTED, item != null && item.isCancelled());
			}

			@Override
			public void updateSelected(boolean selected) {
				super.updateSelected(selected);
				if (selected && getItem().isCancelled()) {
					pseudoClassStateChanged(FxConstants.PC_HIGHLIGHTED_SELECTED, true);
					pseudoClassStateChanged(FxConstants.PC_HIGHLIGHTED, false);
				} else {
					pseudoClassStateChanged(FxConstants.PC_HIGHLIGHTED_SELECTED, false);
				}
			}
		});

		TableFilter<Race> racesTblFilter = TableFilter.forTableView(this.racesTbl).apply();
		racesTblFilter.setSearchStrategy((input, target) -> target.contains(input));

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
	void handleImportAltRegsBtnOnAction() {
		File file = FxUtils.showOpenDialog(getWindow(), null, "Meldungen XML Datei", "*.xml");

		if (file != null) {
			try (FileInputStream fileIn = new FileInputStream(file)) {
				Map<String, List<TMeldung>> altMeldungenByRace = new HashMap<>();

				RegattaMeldungen regattaMeldungen = XMLDataLoader.loadRegattaMeldungen(fileIn);
				List<TRennen> tRennenList = regattaMeldungen.getMeldungen().getRennen();
				for (int i = 0; i < tRennenList.size(); i++) {
					TRennen tRennen = tRennenList.get(i);
					List<TMeldung> altMeldungen = tRennen.getMeldung().stream()
							.filter(rennen -> StringUtils.isNotBlank(rennen.getAlternativeZu()))
							.collect(Collectors.toList());

					if (!altMeldungen.isEmpty()) {
						altMeldungenByRace.computeIfAbsent(tRennen.getNummer(), raceNr -> new ArrayList<>())
								.addAll(altMeldungen);
					}
				}
			} catch (IOException | JAXBException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			}
		}
	}

	@FXML
	void handleRefreshOnAction() {
		loadRaces(true);
	}

	@FXML
	void handleSetDistancesOnAction() {
		disableButtons(true);

		super.dbTaskRunner.runInTransaction(monitor -> this.regattaDAO.setDistances(), dbResult -> {
			try {
				List<Race> races = dbResult.getResult();
				if (races.isEmpty()) {
					FxUtils.showInfoDialog(getWindow(), "Keine Rennen geändert.");
				} else {
					loadRaces(true);
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
	void handleSetMastersAgeClassesOnAction() {
		disableButtons(true);

		super.dbTaskRunner.runInTransaction(monitor -> this.regattaDAO.enableMastersAgeClasses(), dbResult -> {
			try {
				List<Race> races = dbResult.getResult();
				if (races.isEmpty()) {
					FxUtils.showInfoDialog(getWindow(), "Keine Masters Rennen geändert.");
				} else {
					loadRaces(true);
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
		Race selectedItem = this.racesTbl.getSelectionModel().getSelectedItem();

		super.dbTaskRunner.run(monitor -> {
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
				disableButtons(false);
			}
		});
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.setDistancesBtn.setDisable(disabled);
		this.setMastersAgeClassesBtn.setDisable(disabled);
		this.importAltRegsBtn.setDisable(disabled);
		this.racesTbl.setDisable(disabled);
		this.regsTbl.setDisable(disabled);
	}

}
