package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.controlsfx.control.SearchableComboBox;

import de.regatta_hd.aquarius.SetListEntry;
import de.regatta_hd.aquarius.model.Crew;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.Result;
import de.regatta_hd.ui.util.FxUtils;
import de.regatta_hd.ui.util.RaceStringConverter;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Pair;

public class SetRaceController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(SetRaceController.class.getName());
	private static final String FULL_GRAPH = "race-to-results";
	private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

	@FXML
	private SearchableComboBox<Race> raceCbo;
	@FXML
	private VBox srcRaceVBox;
	@FXML
	private TableView<SetListEntry> setListTbl;
	@FXML
	private Label srcCrewLbl;
	@FXML
	private TableView<Crew> srcCrewTbl;
	@FXML
	private Label crewLbl;
	@FXML
	private TableView<Crew> crewTbl;
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
		disableButtons();

		this.setListTbl.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldSelection, newSelection) -> handleSetListSelectedItemChanged(newSelection));

		this.dbTask.run(progress -> {
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
						Race race2 = srcRaces.get(getSrcRaceNumber(race));
						return race2 != null && race2.isOfficial();
					}).collect(Collectors.toList());
			return FXCollections.observableArrayList(filteredRaces);
		}, dbResult -> {
			try {
				this.raceCbo.setItems(dbResult.getResult());
				this.raceCbo.setDisable(false);
				this.setListTbl.setDisable(false);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			}
		});
	}

	private void handleSetListSelectedItemChanged(SetListEntry newSelection) {
		if (newSelection != null) {
			final SetListEntry entry = this.setListTbl.getSelectionModel().getSelectedItem();
			// clear tables
			this.srcCrewTbl.getItems().clear();
			this.crewTbl.getItems().clear();

			// then load new crew lists from DB
			this.dbTask.run(progress -> {
				Set<Crew> srcCrew = entry.getSrcRegistration() != null ? entry.getSrcRegistration().getCrews() : null;
				List<Crew> crews = entry.getRegistration() != null ? entry.getRegistration().getFinalCrews() : null;
				if (srcCrew != null) {
					srcCrew.forEach(Crew::getAthlet);
				}
				if (crews != null) {
					crews.forEach(Crew::getAthlet);
				}
				return new Pair<>(srcCrew, crews);
			}, (dbResult -> {
				try {
					Pair<Set<Crew>, List<Crew>> result = dbResult.getResult();

					if (result.getKey() != null) {
						this.srcCrewTbl.getItems().setAll(result.getKey());
						this.srcCrewLbl.setText(createCrewsLabel(entry, entry.getSrcRegistration()));
						FxUtils.autoResizeColumns(this.srcCrewTbl);
					} else {
						this.srcCrewLbl.setText(getText("SetRaceView.noBoat"));
					}

					if (result.getValue() != null) {
						this.crewTbl.getItems().setAll(result.getValue());
						this.crewLbl.setText(createCrewsLabel(entry, entry.getRegistration()));
						FxUtils.autoResizeColumns(this.crewTbl);
					} else {
						this.crewLbl.setText(getText("SetRaceView.noBoat"));
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				}
			}));
		} else {
			this.srcCrewTbl.getItems().clear();
			this.crewTbl.getItems().clear();
		}
	}

	@FXML
	private void handleRaceSelectedOnAction(ActionEvent event) {
		if (!event.isConsumed() && event.getSource() == this.raceCbo) {
			event.consume();

			this.raceVBox.getChildren().clear();
			this.srcRaceVBox.getChildren().clear();
			this.setListTbl.getItems().clear();

			Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();
			if (selectedRace != null) {
				// remove onAction eventhandler to avoid multiple calls -> workaround
				this.raceCbo.setOnAction(null);

				this.dbTask.run(progress -> {
					Race race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
					Race srcRace = this.regattaDAO.getRace(getSrcRaceNumber(selectedRace), FULL_GRAPH);
					return new Race[] { srcRace, race };
				}, dbResult -> {
					Race race = null;
					try {
						race = dbResult.getResult()[1];
						showSrcRace(dbResult.getResult()[0]);
						showRace(race);
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
						FxUtils.showErrorMessage(getWindow(), e);
					} finally {
						enableButtons(race);

						// attach onAction eventhandler to get further events
						this.raceCbo.setOnAction(this::handleRaceSelectedOnAction);
					}
				});
			}
		}
	}

	@FXML
	private void handleRefreshOnAction() {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();
		if (selectedRace != null) {
			disableButtons();

			this.dbTask.run(progress -> {
				this.db.getEntityManager().clear();
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				Race srcRace = this.regattaDAO.getRace(getSrcRaceNumber(race), FULL_GRAPH);
				return new Race[] { srcRace, race };
			}, dbResult -> {
				Race race = null;
				try {
					race = dbResult.getResult()[1];
					showSrcRace(dbResult.getResult()[0]);
					showRace(race);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					enableButtons(race);
				}
			});
		}
	}

	@FXML
	private void handleCreateSetListOnAction() {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();

		if (selectedRace != null) {
			disableButtons();

			AtomicReference<Race> raceRef = new AtomicReference<>();
			this.dbTask.run(progress -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				Race srcRace = this.regattaDAO.getRace(getSrcRaceNumber(race), FULL_GRAPH);
				raceRef.set(race);
				return this.regattaDAO.createSetList(race, srcRace);
			}, dbResult -> {
				try {
					this.setListTbl.setItems(FXCollections.observableArrayList(dbResult.getResult()));
					FxUtils.autoResizeColumns(this.setListTbl);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					enableButtons(raceRef.get());
				}
			});
		}
	}

	@FXML
	private void handleDeleteSetListOnAction() {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();

		if (selectedRace != null) {
			disableButtons();
			this.setListTbl.getItems().clear();

			this.dbTask.run(progress -> this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH), dbResult -> {
				Race race = null;
				try {
					race = dbResult.getResult();
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					enableButtons(race);
				}
			});
		}
	}

	@FXML
	private void handleSetRaceOnAction() {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();

		if (selectedRace != null && !this.setListTbl.getItems().isEmpty()) {
			disableButtons();

			this.dbTask.runInTransaction(progress -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				this.regattaDAO.setRaceHeats(race, this.setListTbl.getItems());

				this.db.getEntityManager().clear();
				race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				return race;
			}, dbResult -> {
				Race race = null;
				try {
					race = dbResult.getResult();
					showRace(race);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					enableButtons(race);
				}
			});
		}
	}

	@FXML
	private void handleDeleteOnAction() {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();

		if (selectedRace != null && FxUtils.showConfirmDialog(getWindow(), getText("SetRaceView.confirmDelete.title"),
				getText("SetRaceView.confirmDelete.question"))) {
			disableButtons();

			this.dbTask.runInTransaction(progress -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), FULL_GRAPH);
				this.regattaDAO.cleanRaceHeats(race);
				return race;
			}, dbResult -> {
				Race race = null;
				try {
					race = dbResult.getResult();
					showRace(race);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					enableButtons(race);
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

		// build race label text with details from race
		String labelText = new RaceStringConverter().toString(race);
		vbox.getChildren().add(new Label(labelText));

		// loops over all heats of race and reads required data from DB
		race.getHeats().forEach(heat -> {
//			SortedList<HeatRegistration> sortedList = new SortedList<>(
//					FXCollections.observableArrayList(heat.getEntries()));

			Label heatNrLabel = new Label(
					getText("SetRaceView.heatNrLabel.text", Short.valueOf(heat.getDevisionNumber())));
			TableView<HeatRegistration> compEntriesTable = createTableView(withResult);
			compEntriesTable.setItems(FXCollections.observableArrayList(heat.getEntries()));
			compEntriesTable.sort();
			FxUtils.autoResizeColumns(compEntriesTable);

//			sortedList.comparatorProperty().bind(compEntriesTable.comparatorProperty());

			vbox.getChildren().addAll(heatNrLabel, compEntriesTable);
		});
	}

	private void enableButtons(Race race) {
		if (race != null) {
			boolean isSet = race.getSet() != null && race.getSet().booleanValue();
			// disable setRace button if race is already set or set list is empty
			this.setRaceBtn.setDisable(isSet || this.setListTbl.getItems().isEmpty());
			this.deleteBtn.setDisable(!isSet);
		} else {
			this.setRaceBtn.setDisable(false);
			this.deleteBtn.setDisable(false);
		}

		this.createSetListBtn.setDisable(!this.setListTbl.getItems().isEmpty());
		this.deleteSetListBtn.setDisable(this.setListTbl.getItems().isEmpty());
		this.refreshBtn.setDisable(false);
	}

	private void disableButtons() {
		this.setRaceBtn.setDisable(true);
		this.deleteBtn.setDisable(true);
		this.createSetListBtn.setDisable(true);
		this.deleteSetListBtn.setDisable(true);
		this.refreshBtn.setDisable(true);
	}

	private TableView<HeatRegistration> createTableView(boolean withResult) {
		TableView<HeatRegistration> heatRegsTbl = new TableView<>();

		if (!withResult) {
			heatRegsTbl.setRowFactory(tv -> {
				TableRow<HeatRegistration> row = new TableRow<>();
				row.setOnDragDetected(event -> {
					Integer index = row.getIndex();
					Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
					db.setDragView(row.snapshot(null, null));
					ClipboardContent cc = new ClipboardContent();
					cc.put(SERIALIZED_MIME_TYPE, index);
					db.setContent(cc);
					event.consume();
				});

				row.setOnDragOver(event -> {
					Dragboard db = event.getDragboard();
					if (db.hasContent(SERIALIZED_MIME_TYPE)
							&& row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
						event.acceptTransferModes(TransferMode.MOVE);
						event.consume();
					}

				});

				row.setOnDragDropped(event -> {
					Dragboard db = event.getDragboard();
					if (db.hasContent(SERIALIZED_MIME_TYPE)) {
						int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
						TableRow<HeatRegistration> sourceRow = (TableRow<HeatRegistration>) event.getGestureSource();
						TableView<HeatRegistration> tableView = sourceRow.getTableView();
						ObservableList<HeatRegistration> items = tableView.getItems();
						HeatRegistration draggedEntry = items.remove(draggedIndex);

						int dropIndex;

						if (row.isEmpty()) {
							dropIndex = heatRegsTbl.getItems().size();
						} else {
							dropIndex = row.getIndex();
						}

						heatRegsTbl.getItems().add(dropIndex, draggedEntry);

						event.setDropCompleted(true);
						heatRegsTbl.getSelectionModel().select(dropIndex);
						event.consume();
					}
				});
				return row;
			});
		}

		TableColumn<HeatRegistration, Number> bibCol = new TableColumn<>(getText("common.bibAbr"));
		bibCol.setStyle("-fx-alignment: CENTER;");
		bibCol.setCellValueFactory(row -> {
			Registration entry = row.getValue().getRegistration();
			if (entry != null && entry.getBib() != 0) {
				return new SimpleIntegerProperty(entry.getBib());
			}
			return null;
		});

		TableColumn<HeatRegistration, String> boatCol = new TableColumn<>(getText("common.boat"));
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
			rankCol = new TableColumn<>(getText("common.rank"));
			rankCol.setStyle("-fx-alignment: CENTER;");
			rankCol.setCellValueFactory(row -> {
				Result result = row.getValue().getFinalResult();
				if (result != null) {
					return new SimpleIntegerProperty(result.getRank());
				}
				return null;
			});

			resultCol = new TableColumn<>(getText("common.result"));
			resultCol.setStyle("-fx-alignment: CENTER_RIGHT;");
			resultCol.setCellValueFactory(row -> {
				Result result = row.getValue().getFinalResult();
				if (result != null) {
					return new SimpleStringProperty(result.getDisplayValue());
				}
				return null;
			});

			heatRegsTbl.getColumns().add(rankCol);
			heatRegsTbl.getSortOrder().add(resultCol);
		}

		heatRegsTbl.getColumns().add(bibCol);
		heatRegsTbl.getColumns().add(boatCol);

		if (resultCol != null) {
			heatRegsTbl.getColumns().add(resultCol);
		}

		return heatRegsTbl;
	}

	private Window getWindow() {
		return this.refreshBtn.getScene().getWindow();
	}

	// static helpers

	private static String createCrewsLabel(SetListEntry entry, Registration registration) {
		return registration.getRace().getNumber() + " - " + registration.getBib() + " " + entry.getBoat();
	}

	private static String getSrcRaceNumber(Race race) {
		return replaceChar(race.getNumber(), '1', 0);
	}

	private static String replaceChar(String str, char ch, int index) {
		return str.substring(0, index) + ch + str.substring(index + 1);
	}
}
