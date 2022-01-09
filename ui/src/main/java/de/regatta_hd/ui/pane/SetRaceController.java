package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.SetListEntry;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.Result;
import de.regatta_hd.ui.control.FilterComboBox;
import de.regatta_hd.ui.util.FxUtils;
import de.regatta_hd.ui.util.RaceStringConverter;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

public class SetRaceController extends AbstractBaseController {

	private static final String FULL_GRAPH = "race-to-results";

	@Inject
	private RegattaDAO regattaDAO;

	@FXML
	private FilterComboBox<Race> raceCbo;
	@FXML
	private VBox srcRaceVBox;
	@FXML
	private TableView<SetListEntry> setListTbl;
	@FXML
	private VBox raceVBox;

	// toolbar buttons
	@FXML
	private Button refreshBtn;
	@FXML
	private Button createSetListBtn;
	@FXML
	private Button setRaceBtn;
	@FXML
	private Button deleteBtn;

	@FXML
	private Button deleteSetListBtn;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.raceCbo.setDisable(true);
		this.setListTbl.setDisable(true);
		disableButtons(true);

		this.dbTask.run(() -> {
			List<Race> allRaces = this.regattaDAO.getRaces(FULL_GRAPH);
			List<Race> races = new ArrayList<>();
			Map<String, Race> srcRaces = new HashMap<>();
			allRaces.forEach(race -> {
				switch (race.getNumber().charAt(0)) {
				case '1':
					srcRaces.put(race.getNumber(), race);
					break;
				case '2':
					races.add(race);
					break;
				default:
					// ignored
					break;
				}
			});

			List<Race> filteredRaces = races.stream()
					// remove master races, open age class and races with one heat, as they will not be set
					.filter(race -> !race.getAgeClass().isOpen() && !race.getAgeClass().isMasters()
							&& race.getHeats().size() > 1)
					// remove races whose source race result isn't official yet
					.filter(race -> {
						// create race number of source race -> replace 2 with 1
						String srcRaceNumber = getSrcRaceNumber(race);
						Race race2 = srcRaces.get(srcRaceNumber);
						return race2 != null && race2.isOfficial();
					}).toList();
			return FXCollections.observableArrayList(filteredRaces);
		}, dbResult -> {
			try {
				this.raceCbo.setInitialItems(dbResult.getResult());
				this.raceCbo.setDisable(false);
				this.setListTbl.setDisable(false);
			} catch (Exception e) {
				FxUtils.showErrorMessage(e);
			}
		});
	}

	@FXML
	private void handleTargetOfferOnAction() {
		this.raceVBox.getChildren().clear();
		this.srcRaceVBox.getChildren().clear();
		this.setListTbl.getItems().clear();

		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();
		if (selectedRace != null) {
			this.dbTask.run(() -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				Race srcRace = this.regattaDAO.getRace(getSrcRaceNumber(selectedRace), FULL_GRAPH);
				return new Race[] { srcRace, race };
			}, dbResult -> {
				try {
					Race[] races = dbResult.getResult();
					showSrcRace(races[0]);
					showRace(races[1]);
				} catch (Exception e) {
					FxUtils.showErrorMessage(e);
					disableButtons(false);
				}
			});
		}
	}

	@FXML
	private void handleRefreshOnAction() {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();
		if (selectedRace != null) {
			disableButtons(true);

			this.dbTask.run(() -> {
				this.db.getEntityManager().clear();
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				race = this.regattaDAO.getRace(getSrcRaceNumber(race), FULL_GRAPH);
				return race;
			}, dbResult -> {
				try {
					dbResult.getResult();
					handleTargetOfferOnAction();
				} catch (Exception e) {
					FxUtils.showErrorMessage(e);
					disableButtons(false);
				}
			});
		}
	}

	@FXML
	private void handleCreateSetListOnAction() {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();

		if (selectedRace != null) {
			disableButtons(true);

			this.dbTask.run(() -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				Race srcRace = this.regattaDAO.getRace(getSrcRaceNumber(race), FULL_GRAPH);
				return this.regattaDAO.createSetList(race, srcRace);
			}, dbResult -> {
				try {
					this.setListTbl.setItems(FXCollections.observableArrayList(dbResult.getResult()));
					FxUtils.autoResizeColumns(this.setListTbl);
				} catch (Exception e) {
					FxUtils.showErrorMessage(e);
				} finally {
					disableButtons(false);
				}
			});
		}
	}

	@FXML
	private void handleSetRaceOnAction() {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();

		if (selectedRace != null && !this.setListTbl.getItems().isEmpty()) {
			disableButtons(true);

			this.dbTask.runInTransaction(() -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				this.regattaDAO.setRaceHeats(race, this.setListTbl.getItems());

				this.db.getEntityManager().clear();
				race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				return race;
			}, dbResult -> {
				try {
					showRace(dbResult.getResult());
				} catch (Exception e) {
					FxUtils.showErrorMessage(e);
					disableButtons(false);
				}
			});
		}
	}

	@FXML
	private void handleDeleteSetListOnAction() {
		disableButtons(true);
		this.setListTbl.getItems().clear();
		disableButtons(false);
	}

	@FXML
	private void handleDeleteOnAction() {
		disableButtons(true);

		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();
		if (selectedRace != null) {
			this.dbTask.runInTransaction(() -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				this.regattaDAO.cleanRaceHeats(race);
				return race;
			}, dbResult -> {
				try {
					showRace(dbResult.getResult());
				} catch (Exception e) {
					FxUtils.showErrorMessage(e);
					disableButtons(false);
				}
			});
		}
	}

	// JavaFX stuff

	private void showSrcRace(Race srcRace) {
		showRace(srcRace, this.srcRaceVBox, true);
	}

	private void showRace(Race race) {
		showRace(race, this.raceVBox, false);
	}

	private void showRace(Race race, VBox vbox, boolean withResult) {
		vbox.getChildren().clear();

		this.dbTask.run(() -> {
			Map<Short, SortedList<HeatRegistration>> result = new HashMap<>();

			// loops over all heats of race and reads required data from DB
			race.getHeats().forEach(heat -> {
				Set<HeatRegistration> entries = heat.getEntries();
				entries.forEach(entry -> {
					entry.getResults();
					entry.getRegistration().getClub().getAbbreviation();
					entry.getFinalResult();
				});
				SortedList<HeatRegistration> sortedList = new SortedList<>(FXCollections.observableArrayList(entries));
				result.put(heat.getHeatNumber(), sortedList);
			});

			// build race label text with details from race
			String labelText = new RaceStringConverter().toString(race);
			return new Pair<>(labelText, result);
		}, pair -> {
			try {
				Pair<String, Map<Short, SortedList<HeatRegistration>>> result = pair.getResult();
				vbox.getChildren().add(new Label(result.getKey()));

				race.getHeats().forEach(heat -> {
					SortedList<HeatRegistration> sortedList = result.getValue().get(heat.getHeatNumber());
					Label heatNrLabel = new Label(getText("SetRaceView.heatNrLabel.text", heat.getHeatNumber()));
					TableView<HeatRegistration> compEntriesTable = createTableView(withResult);
					compEntriesTable.setItems(sortedList);
					FxUtils.autoResizeColumns(compEntriesTable);

					sortedList.comparatorProperty().bind(compEntriesTable.comparatorProperty());
					vbox.getChildren().addAll(heatNrLabel, compEntriesTable);
				});
			} catch (Exception e) {
				FxUtils.showErrorMessage(e);
			} finally {
				// false if showing race
				disableButtons(withResult);
			}
		});
	}

	private void disableButtons(boolean disabled) {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();
		if (selectedRace != null) {
			this.dbTask.run(() -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber());
				return race.getSet();
			}, raceIsSet -> {
				try {
					boolean isSet = raceIsSet != null && raceIsSet.getResult().booleanValue();
					// disable setRace button if race is already set or set list is empty
					this.setRaceBtn.setDisable(disabled || isSet || this.setListTbl.getItems().isEmpty());
					this.deleteBtn.setDisable(disabled || !isSet);
				} catch (Exception e) {
					FxUtils.showErrorMessage(e);
				}
			});
		} else {
			this.setRaceBtn.setDisable(disabled);
			this.deleteBtn.setDisable(disabled);
		}

		this.createSetListBtn.setDisable(disabled || !this.setListTbl.getItems().isEmpty());
		this.deleteSetListBtn.setDisable(disabled || this.setListTbl.getItems().isEmpty());
		this.refreshBtn.setDisable(disabled);
	}

	private TableView<HeatRegistration> createTableView(boolean withResult) {
		TableView<HeatRegistration> heatRegsTbl = new TableView<>();

		TableColumn<HeatRegistration, Number> bibCol = new TableColumn<>(
				getText("SetRaceView.heatRegsTbl.bibCol.text"));
		bibCol.setStyle("-fx-alignment: CENTER;");
		bibCol.setCellValueFactory(row -> {
			Registration entry = row.getValue().getRegistration();
			if (entry != null && entry.getBib() != null) {
				return new SimpleIntegerProperty(entry.getBib().shortValue());
			}
			return null;
		});

		TableColumn<HeatRegistration, String> boatCol = new TableColumn<>(
				getText("SetRaceView.heatRegsTbl.boatCol.text"));
		boatCol.setCellValueFactory(row -> {
			Registration entry = row.getValue().getRegistration();
			if (entry != null && entry.getClub() != null) {
				String value = entry.getClub().getAbbreviation();
				if (entry.getBoatNumber() != null) {
					value += " - Boot " + entry.getBoatNumber();
				}
				return new SimpleStringProperty(value);
			}
			return null;
		});

		TableColumn<HeatRegistration, Number> rankCol = null;
		TableColumn<HeatRegistration, String> resultCol = null;
		if (withResult) {
			rankCol = new TableColumn<>(getText("SetRaceView.heatRegsTbl.rankCol.text"));
			rankCol.setSortType(SortType.ASCENDING);
			rankCol.setSortable(true);
			rankCol.setStyle("-fx-alignment: CENTER;");
			rankCol.setCellValueFactory(row -> {
				Result result = row.getValue().getFinalResult();
				if (result != null) {
					return new SimpleIntegerProperty(result.getRank());
				}
				return null;
			});

			resultCol = new TableColumn<>(getText("SetRaceView.heatRegsTbl.resultCol.text"));
			resultCol.setStyle("-fx-alignment: CENTER_RIGHT;");
			resultCol.setCellValueFactory(row -> {
				Result result = row.getValue().getFinalResult();
				if (result != null) {
					return new SimpleStringProperty(result.getDisplayValue());
				}
				return null;
			});

			heatRegsTbl.getColumns().add(rankCol);
		}

		if (rankCol != null) {
			heatRegsTbl.getSortOrder().add(rankCol);
		}

		heatRegsTbl.getColumns().add(bibCol);
		heatRegsTbl.getColumns().add(boatCol);

		if (resultCol != null) {
			heatRegsTbl.getColumns().add(resultCol);
		}

		return heatRegsTbl;
	}

	private static String getSrcRaceNumber(Race race) {
		return replaceChar(race.getNumber(), '1', 0);
	}

	private static String replaceChar(String str, char ch, int index) {
		return str.substring(0, index) + ch + str.substring(index + 1);
	}
}
