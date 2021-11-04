package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.Result;
import de.regatta_hd.ui.control.FilterComboBox;
import de.regatta_hd.ui.util.DBTask;
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

	@FXML
	private FilterComboBox<Race> raceCbo;
	@FXML
	private VBox srcRaceVBox;
	@FXML
	private VBox raceVBox;
	@FXML
	private Button setRaceBtn;
	@FXML
	private Button deleteBtn;
	@FXML
	private Button refreshBtn;

	@Inject
	private RegattaDAO regattaDAO;
	@Inject
	private AquariusDB db;
	@Inject
	private DBTask dbTask;

	private Race srcRace;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.raceCbo.setDisable(true);
		disableButtons(true);

		this.dbTask.run(() -> {
			List<Race> races = this.regattaDAO.findRaces("2%");
			// remove master races and races with one heat, as they will not be set
			List<Race> filteredRaces = races.stream()
					.filter(race -> !race.getAgeClass().isMasters() && race.getHeats().size() > 1).toList();
			return FXCollections.observableArrayList(filteredRaces);
		}, races -> {
			this.raceCbo.setInitialItems(races);
			this.raceCbo.setDisable(false);
		});
	}

	@FXML
	private void handleTargetOfferOnAction() {
		this.raceVBox.getChildren().clear();
		this.srcRaceVBox.getChildren().clear();

		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();

		this.dbTask.run(() -> {
			if (selectedRace != null) {
				String srcRaceNumber = replaceChar(selectedRace.getNumber(), '1', 0);
				this.srcRace = this.regattaDAO.getRace(srcRaceNumber);
				return this.srcRace;
			}
			return null;
		}, result -> {
			showSrcRace();
			showRace();
		});
	}

	@FXML
	private void handleRefreshOnAction() {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();
		if (selectedRace != null) {
			disableButtons(true);

			this.dbTask.run(() -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber());
				this.db.getEntityManager().refresh(race);
				race = this.regattaDAO.getRace(this.srcRace.getNumber());
				this.db.getEntityManager().refresh(race);
				return null;
			}, result -> {
				handleTargetOfferOnAction();
			});
		}
	}

	@FXML
	private void handleSetRaceOnAction() {
		disableButtons(true);

		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();
		if (selectedRace != null && this.srcRace != null) {
			this.dbTask.runInTransaction(() -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber());
				this.regattaDAO.setRaceHeats(race, this.srcRace);
				return null;
			}, result -> {
				showRace();
			});
		}
	}

	@FXML
	private void handleDeleteOnAction() {
		disableButtons(true);

		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();
		if (selectedRace != null) {
			this.dbTask.runInTransaction(() -> {
				Race race = this.regattaDAO.getRace(selectedRace.getNumber());
				this.regattaDAO.cleanRaceHeats(race);
				return null;
			}, result -> {
				showRace();
			});
		}
	}

	// JavaFX stuff

	private void showSrcRace() {
		if (this.srcRace != null) {
			this.dbTask.run(() -> this.regattaDAO.getRace(this.srcRace.getNumber()),
					race -> showRace(race, this.srcRaceVBox, true));
		}
	}

	private void showRace() {
		Race selectedRace = this.raceCbo.getSelectionModel().getSelectedItem();
		if (selectedRace != null) {
			this.dbTask.run(() -> this.regattaDAO.getRace(selectedRace.getNumber()),
					race -> showRace(race, this.raceVBox, false));
		}
	}

	private void showRace(Race race, VBox vbox, boolean withResult) {
		vbox.getChildren().clear();

		this.dbTask.run(() -> {
			Map<Short, SortedList<HeatRegistration>> result = new HashMap<>();

			// loops over all heats of race and reads required data from DB
			race.getHeats().forEach(heat -> {
				List<HeatRegistration> entries = heat.getEntries();
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
			vbox.getChildren().add(new Label(pair.getKey()));

			race.getHeats().forEach(heat -> {
				SortedList<HeatRegistration> sortedList = pair.getValue().get(heat.getHeatNumber());
				Label heatNrLabel = new Label(getText("SetRaceView.heatNrLabel.text", heat.getHeatNumber()));
				TableView<HeatRegistration> compEntriesTable = createTableView(withResult);
				compEntriesTable.setItems(sortedList);
				sortedList.comparatorProperty().bind(compEntriesTable.comparatorProperty());
				vbox.getChildren().addAll(heatNrLabel, compEntriesTable);
			});

			// false if showing race
			disableButtons(withResult);
		});
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.deleteBtn.setDisable(disabled);
		this.setRaceBtn.setDisable(disabled);
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
					return new SimpleIntegerProperty(result.getRank().byteValue());
				}
				return null;
			});

			resultCol = new TableColumn<>(getText("SetRaceView.heatRegsTbl.resultCol.text"));
			resultCol.setStyle("-fx-alignment: CENTER_RIGHT;");
			resultCol.setCellValueFactory(row -> {
				List<Result> results = row.getValue().getResults();
				for (Result result : results) {
					if (result.getSplitNr() == 64) {
						return new SimpleStringProperty(result.getDisplayValue());
					}
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

	private static String replaceChar(String str, char ch, int index) {
		return str.substring(0, index) + ch + str.substring(index + 1);
	}
}
