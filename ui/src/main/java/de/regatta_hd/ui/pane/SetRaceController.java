package de.regatta_hd.ui.pane;

import static de.regatta_hd.commons.fx.util.FxConstants.FX_ALIGNMENT_CENTER;
import static de.regatta_hd.commons.fx.util.FxConstants.FX_ALIGNMENT_CENTER_RIGHT;
import static java.util.Objects.nonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.util.Pair;

import org.controlsfx.control.SearchableComboBox;

import de.regatta_hd.aquarius.SeedingListEntry;
import de.regatta_hd.aquarius.model.Crew;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Race.GroupMode;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.Result;
import de.regatta_hd.aquarius.util.ModelUtils;
import de.regatta_hd.commons.fx.util.FxConstants;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.ui.util.RaceStringConverter;

public class SetRaceController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(SetRaceController.class.getName());
	private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

	@FXML
	private SearchableComboBox<Race> racesCbo;
	@FXML
	private VBox srcRaceVBox;

	// seeding list table
	@FXML
	private TableView<SeedingListEntry> seedingListTbl;
	@FXML
	private TableColumn<SeedingListEntry, Integer> seedingListPosCol;
	@FXML
	private TableColumn<SeedingListEntry, Integer> seedingListBibCol;
	@FXML
	private TableColumn<SeedingListEntry, String> seedingListBoatCol;
	@FXML
	private TableColumn<SeedingListEntry, Integer> seedingListRankCol;
	@FXML
	private TableColumn<SeedingListEntry, Integer> seedingListDivisionNumberCol;
	@FXML
	private TableColumn<SeedingListEntry, String> seedingListResultCol;
	@FXML
	private TableColumn<SeedingListEntry, Boolean> seedingListEqualCrewCol;

	// source crew table
	@FXML
	private Label srcCrewLbl;
	@FXML
	private TableView<Crew> srcCrewTbl;
	@FXML
	private TableColumn<Crew, Byte> srcCrewPosCol;
	@FXML
	private TableColumn<Crew, Boolean> srcCrewCoxCol;
	@FXML
	private TableColumn<Crew, String> srcCrewNameCol;

	// crew table
	@FXML
	private Label crewLbl;
	@FXML
	private TableView<Crew> crewTbl;
	@FXML
	private TableColumn<Crew, Byte> crewPosCol;
	@FXML
	private TableColumn<Crew, Boolean> crewCoxCol;
	@FXML
	private TableColumn<Crew, String> crewNameCol;

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

	private final ObservableList<Race> racesList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.racesCbo.setItems(this.racesList);
		this.racesCbo.setDisable(true);
		this.seedingListTbl.setDisable(true);
		disableButtons();

		this.seedingListTbl.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldSelection, newSelection) -> handleSetListSelectedItemChanged(newSelection));

		DoubleBinding usedWidth = this.srcCrewPosCol.widthProperty().add(this.srcCrewCoxCol.widthProperty());
		this.srcCrewNameCol.prefWidthProperty()
				.bind(this.srcCrewTbl.widthProperty().subtract(usedWidth).subtract(FxConstants.TABLE_BORDER_WIDTH));

		usedWidth = this.crewPosCol.widthProperty().add(this.crewCoxCol.widthProperty());
		this.crewNameCol.prefWidthProperty()
				.bind(this.crewTbl.widthProperty().subtract(usedWidth).subtract(FxConstants.TABLE_BORDER_WIDTH));

		usedWidth = this.seedingListPosCol.widthProperty().add(this.seedingListBibCol.widthProperty())
				.add(this.seedingListRankCol.widthProperty())
				.add(this.seedingListDivisionNumberCol.widthProperty().add(this.seedingListResultCol.widthProperty())
						.add(this.seedingListEqualCrewCol.widthProperty()));
		this.seedingListBoatCol.prefWidthProperty()
				.bind(this.seedingListTbl.widthProperty().subtract(usedWidth).subtract(FxConstants.TABLE_BORDER_WIDTH));

		loadRaces();
	}

	@Override
	protected void onActiveRegattaChanged(Regatta activeRegatta) {
		if (activeRegatta != null) {
			loadRaces();
		} else {
			clearRaces();
		}
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("PrimaryView.setRaceMitm.text") + " - " + activeRegatta.getTitle()
				: getText("PrimaryView.setRaceMitm.text");
	}

	private void loadRaces() {
		clearRaces();

		super.dbTaskRunner.run(progress -> {
			List<Race> allRaces = this.regattaDAO.getRaces(Race.GRAPH_RESULTS);
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
					// remove master races, open age class and races with registrations for one heat only, as they will
					// not be set
					.filter(race -> !race.getAgeClass().isOpen() // don't set open races
							&& !race.getAgeClass().isMasters() // don't set master races
							&& race.getGroupMode() == GroupMode.NONE // don't set races with age groups
							&& race.getActiveRegistrations() // don't set races with only one devision
									.count() > (race.getRaceMode() != null ? race.getRaceMode().getLaneCount() : 4))
					// remove races whose source race result isn't official yet
					.filter(race -> {
						// create race number of source race -> replace 2 with 1
						Race srcRace = srcRaces.get(getSrcRaceNumber(race));
						// the source race needs to be driven with an official result
						return srcRace != null && srcRace.isOfficial() && !srcRace.isCancelled();
					}).toList();
			return FXCollections.observableArrayList(filteredRaces);
		}, dbResult -> {
			try {
				this.racesList.addAll(dbResult.getResult());
				this.racesCbo.setDisable(false);
				this.seedingListTbl.setDisable(false);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			}
		});
	}

	private void handleSetListSelectedItemChanged(SeedingListEntry newSelection) {
		if (newSelection != null) {
			final SeedingListEntry entry = this.seedingListTbl.getSelectionModel().getSelectedItem();
			// clear tables
			this.srcCrewTbl.getItems().clear();
			this.crewTbl.getItems().clear();

			// then load new crew lists from DB
			super.dbTaskRunner.run(progress -> {
				List<Crew> srcCrew = entry.getSrcRegistration() != null ? entry.getSrcRegistration().getFinalCrews()
						: null;
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
					Pair<List<Crew>, List<Crew>> result = dbResult.getResult();

					if (result.getKey() != null) {
						this.srcCrewTbl.getItems().setAll(result.getKey());
						this.srcCrewLbl.setText(createCrewsLabel(entry.getSrcRegistration()));
					} else {
						this.srcCrewLbl.setText(getText("SetRaceView.noBoat"));
					}

					if (result.getValue() != null) {
						this.crewTbl.getItems().setAll(result.getValue());
						this.crewLbl.setText(createCrewsLabel(entry.getRegistration()));
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
		if (!event.isConsumed() && event.getSource() == this.racesCbo) {
			event.consume();

			this.raceVBox.getChildren().clear();
			this.srcRaceVBox.getChildren().clear();
			this.seedingListTbl.getItems().clear();

			Race selectedRace = this.racesCbo.getSelectionModel().getSelectedItem();
			if (selectedRace != null) {
				// remove onAction eventhandler to avoid multiple calls -> workaround
				this.racesCbo.setOnAction(null);

				super.dbTaskRunner.run(progress -> {
					Race race = this.regattaDAO.getRace(selectedRace.getNumber(), Race.GRAPH_RESULTS);
					Race srcRace = this.regattaDAO.getRace(getSrcRaceNumber(selectedRace), Race.GRAPH_RESULTS);
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
						this.racesCbo.setOnAction(this::handleRaceSelectedOnAction);
					}
				});
			} else {
				enableButtons(null);
			}
		}
	}

	@FXML
	private void handleRefreshOnAction() {
		Race selectedRace = this.racesCbo.getSelectionModel().getSelectedItem();
		if (selectedRace != null) {
			disableButtons();

			super.dbTaskRunner.run(progress -> {
				this.db.getEntityManager().clear();
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), Race.GRAPH_RESULTS);
				Race srcRace = this.regattaDAO.getRace(getSrcRaceNumber(race), Race.GRAPH_RESULTS);
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
		Race selectedRace = this.racesCbo.getSelectionModel().getSelectedItem();

		if (selectedRace != null) {
			disableButtons();

			AtomicReference<Race> raceRef = new AtomicReference<>();
			super.dbTaskRunner.run(progress -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), Race.GRAPH_RESULTS);
				Race srcRace = this.regattaDAO.getRace(getSrcRaceNumber(race), Race.GRAPH_RESULTS);
				raceRef.set(race);
				return this.regattaDAO.createSeedingList(race, srcRace);
			}, dbResult -> {
				try {
					this.seedingListTbl.setItems(FXCollections.observableArrayList(dbResult.getResult()));
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
		Race selectedRace = this.racesCbo.getSelectionModel().getSelectedItem();

		if (selectedRace != null) {
			disableButtons();
			this.seedingListTbl.getItems().clear();

			super.dbTaskRunner.run(progress -> this.regattaDAO.getRace(selectedRace.getNumber(), Race.GRAPH_RESULTS),
					dbResult -> {
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
		Race selectedRace = this.racesCbo.getSelectionModel().getSelectedItem();

		if (selectedRace != null && !this.seedingListTbl.getItems().isEmpty()) {
			disableButtons();

			super.dbTaskRunner.runInTransaction(progress -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), Race.GRAPH_RESULTS);
				this.regattaDAO.setRaceHeats(race, this.seedingListTbl.getItems());

				this.db.getEntityManager().clear();
				race = this.regattaDAO.getRace(selectedRace.getNumber(), Race.GRAPH_RESULTS);
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
		Race selectedRace = this.racesCbo.getSelectionModel().getSelectedItem();

		if (selectedRace != null && FxUtils.showConfirmDialog(getWindow(), getText("SetRaceView.confirmDelete.title"),
				getText("SetRaceView.confirmDelete.question"))) {
			disableButtons();

			super.dbTaskRunner.runInTransaction(progress -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber(), Race.GRAPH_RESULTS);
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
		race.getDrivenHeats().forEach(heat -> {
			Label heatNrLabel = new Label(
					getText("SetRaceView.heatNrLabel.text", Short.valueOf(heat.getDivisionNumber())));
			TableView<HeatRegistration> compEntriesTable = createTableView(withResult);

			ObservableList<HeatRegistration> items = FXCollections
					.observableArrayList(withResult ? heat.getEntriesSortedByRank() : heat.getEntriesSortedByLane());
			compEntriesTable.setItems(items);
			compEntriesTable.sort();

			vbox.getChildren().addAll(heatNrLabel, compEntriesTable);
		});
	}

	private void enableButtons(Race race) {
		if (race != null) {
			boolean isSet = race.isSet();
			// disable setRace button if race is already set or set list is empty
			this.setRaceBtn.setDisable(isSet || this.seedingListTbl.getItems().isEmpty());
			this.deleteBtn.setDisable(!isSet);
			this.createSetListBtn.setDisable(!this.seedingListTbl.getItems().isEmpty());
		} else {
			this.setRaceBtn.setDisable(true);
			this.deleteBtn.setDisable(true);
			this.createSetListBtn.setDisable(true);
		}

		this.deleteSetListBtn.setDisable(this.seedingListTbl.getItems().isEmpty());
		this.refreshBtn.setDisable(false);
	}

	private void disableButtons() {
		this.setRaceBtn.setDisable(true);
		this.deleteBtn.setDisable(true);
		this.createSetListBtn.setDisable(true);
		this.deleteSetListBtn.setDisable(true);
		this.refreshBtn.setDisable(true);
	}

	private TableView<HeatRegistration> createTableView(boolean sourceTable) {
		TableView<HeatRegistration> heatRegsTbl = new TableView<>();

		if (!sourceTable) {
			heatRegsTbl.setRowFactory(tv -> getHeatRegTableRow());
		}

		TableColumn<HeatRegistration, Number> bibCol = new TableColumn<>(getText("common.bibAbr"));
		bibCol.setStyle(FX_ALIGNMENT_CENTER);
		bibCol.setResizable(false);
		bibCol.setMaxWidth(35);
		bibCol.setSortable(false);
		bibCol.setReorderable(false);
		bibCol.setCellValueFactory(row -> {
			Registration entry = row.getValue().getRegistration();
			if (entry != null && entry.getBib() != null) {
				return new SimpleIntegerProperty(entry.getBib().intValue());
			}
			return null;
		});

		TableColumn<HeatRegistration, String> boatCol = new TableColumn<>(getText("common.boat"));
		boatCol.setReorderable(false);
		boatCol.setSortable(false);
		boatCol.setCellValueFactory(row -> {
			return new SimpleStringProperty(ModelUtils.getBoatLabel(row.getValue()));
		});

		TableColumn<HeatRegistration, Number> rankCol = null;
		TableColumn<HeatRegistration, String> resultCol = null;
		if (sourceTable) {
			rankCol = new TableColumn<>(getText("common.rank"));
			rankCol.setResizable(false);
			rankCol.setReorderable(false);
			rankCol.setSortable(false);
			rankCol.setMaxWidth(35);
			rankCol.setStyle(FX_ALIGNMENT_CENTER);
			rankCol.setCellValueFactory(row -> {
				Result result = row.getValue().getFinalResult();
				if (result != null) {
					return new SimpleIntegerProperty(result.getRank());
				}
				return null;
			});

			resultCol = new TableColumn<>(getText("common.result"));
			resultCol.setResizable(false);
			resultCol.setMaxWidth(60);
			resultCol.setReorderable(false);
			resultCol.setSortable(false);
			resultCol.setStyle(FX_ALIGNMENT_CENTER_RIGHT);
			resultCol.setCellValueFactory(row -> {
				Result result = row.getValue().getFinalResult();
				if (result != null) {
					return new SimpleStringProperty(result.getDisplayValue());
				}
				return null;
			});

			heatRegsTbl.getColumns().add(rankCol);
			heatRegsTbl.getSortOrder().add(resultCol);

			DoubleBinding usedWidth = bibCol.widthProperty().add(rankCol.widthProperty())
					.add(resultCol.widthProperty());
			boatCol.prefWidthProperty()
					.bind(heatRegsTbl.widthProperty().subtract(usedWidth).subtract(FxConstants.TABLE_BORDER_WIDTH));
		} else {
			TableColumn<HeatRegistration, Number> laneCol = null;
			laneCol = new TableColumn<>(getText("common.lane"));
			laneCol.setMaxWidth(35);
			laneCol.setResizable(false);
			laneCol.setReorderable(false);
			laneCol.setSortable(false);
			laneCol.setStyle(FX_ALIGNMENT_CENTER);
			laneCol.setCellValueFactory(row -> new SimpleIntegerProperty(row.getValue().getLane()));

			heatRegsTbl.getColumns().add(laneCol);
			heatRegsTbl.getSortOrder().add(laneCol);

			DoubleBinding usedWidth = bibCol.widthProperty().add(laneCol.widthProperty());
			boatCol.prefWidthProperty()
					.bind(heatRegsTbl.widthProperty().subtract(usedWidth).subtract(FxConstants.TABLE_BORDER_WIDTH));
		}

		heatRegsTbl.getColumns().add(bibCol);
		heatRegsTbl.getColumns().add(boatCol);

		if (resultCol != null) {
			heatRegsTbl.getColumns().add(resultCol);
		}

		return heatRegsTbl;
	}

	private TableRow<HeatRegistration> getHeatRegTableRow() {
		TableRow<HeatRegistration> row = new TableRow<>();

		row.setOnDragDetected(event -> {
			ClipboardContent content = new ClipboardContent();
			content.put(SERIALIZED_MIME_TYPE, Integer.valueOf(row.getIndex()));

			Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
			dragboard.setDragView(row.snapshot(null, null));
			dragboard.setContent(content);

			event.consume();
		});

		row.setOnDragOver(event -> {
			Dragboard dragboard = event.getDragboard();
			if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
				@SuppressWarnings("unchecked")
				TableRow<HeatRegistration> sourceRow = (TableRow<HeatRegistration>) event.getGestureSource();

				@SuppressWarnings("unchecked")
				TableRow<HeatRegistration> targetRow = (TableRow<HeatRegistration>) event.getSource();
				ObservableList<HeatRegistration> targetItems = targetRow.getTableView().getItems();

				boolean accepted = (sourceRow.getTableView() == targetRow.getTableView()
						&& sourceRow.getIndex() != targetRow.getIndex()) || targetItems.size() < 4;
				if (accepted) {
					event.acceptTransferModes(TransferMode.MOVE);
					event.consume();
				}
			}
		});

		row.setOnDragDropped(event -> {
			Dragboard dragboard = event.getDragboard();
			if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
				Integer draggedIndex = (Integer) dragboard.getContent(SERIALIZED_MIME_TYPE);

				@SuppressWarnings("unchecked")
				TableRow<HeatRegistration> sourceRow = (TableRow<HeatRegistration>) event.getGestureSource();
				TableView<HeatRegistration> srcTable = sourceRow.getTableView();
				ObservableList<HeatRegistration> srcItems = srcTable.getItems();

				@SuppressWarnings("unchecked")
				TableRow<HeatRegistration> targetRow = (TableRow<HeatRegistration>) event.getSource();
				TableView<HeatRegistration> targetTable = targetRow.getTableView();
				ObservableList<HeatRegistration> targetItems = targetTable.getItems();

				HeatRegistration draggedEntry = srcItems.remove(draggedIndex.intValue());

				// set new heat at dropped entry
				draggedEntry.setHeat(targetItems.get(0).getHeat());

				int dropIndex = row.isEmpty() ? targetItems.size() : row.getIndex();
				targetItems.add(dropIndex, draggedEntry);

				super.dbTaskRunner.runInTransaction(monitor -> {
					// re-calculate lanes in source heat
					for (short i = 0; i < srcItems.size(); i++) {
						HeatRegistration heatReg = srcItems.get(i);
						heatReg.setLane((short) (i + 1));
						super.db.getEntityManager().merge(heatReg);
					}
					// re-calculate lanes in target heat
					for (short i = 0; i < targetItems.size(); i++) {
						HeatRegistration heatRegistration = targetItems.get(i);
						heatRegistration.setLane((short) (i + 1));
						super.db.getEntityManager().merge(heatRegistration);
					}
					return null;
				}, result -> {
					srcTable.refresh();
					targetTable.refresh();
					event.setDropCompleted(true);
					event.consume();
				});
			}
		});
		return row;
	}

	private void clearRaces() {
		this.racesCbo.getSelectionModel().clearSelection();
		// workaround to avoid exceptions, see: https://stackoverflow.com/q/12142518
		this.racesCbo.setValue(null);
		this.racesList.clear();
	}

	// static helpers

	private static String createCrewsLabel(Registration registration) {
		return registration.getRace().getNumber() + " - " + registration.getBib() + " "
				+ ModelUtils.getBoatLabel(registration);
	}

	private static String getSrcRaceNumber(Race race) {
		return replaceChar(race.getNumber(), '1', 0);
	}

	private static String replaceChar(String str, char ch, int index) {
		return str.substring(0, index) + ch + str.substring(index + 1);
	}
}
