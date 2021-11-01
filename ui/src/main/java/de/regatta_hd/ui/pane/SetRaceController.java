package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class SetRaceController extends AbstractBaseController {

	private final SimpleListProperty<Race> srcRaceProp = new SimpleListProperty<>(FXCollections.observableArrayList());
	@FXML
	private FilterComboBox<Race> raceCbo;
	@FXML
	private ComboBox<Race> srcRaceCbo;
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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.raceCbo.setDisable(true);
		updateControls();

		this.srcRaceCbo.itemsProperty().bind(this.srcRaceProp);

		this.dbTask.run(() -> {
			List<Race> races = this.regattaDAO.findRaces("2%");
			// remove master races and races with one heat, as they will not be set
			List<Race> filteredRaces = races.stream()
					.filter(race -> !race.getAgeClass().isMasters() && race.getHeats().size() > 1).toList();
			return FXCollections.observableArrayList(filteredRaces);
		}, races -> {
			this.raceCbo.setInitialItems(races);
			this.raceCbo.setDisable(false);
			updateControls();
		});
	}

	@FXML
	private void handleTargetOfferOnAction() {
		Race race = this.raceCbo.getSelectionModel().getSelectedItem();
		this.raceVBox.getChildren().clear();

		this.dbTask.run(() -> {
			if (race != null) {
				// get all races with same attributes
				return this.regattaDAO.findRaces("1%", race.getBoatClass(), race.getAgeClass(), race.isLightweight())
						.stream().filter(srcRace -> race.getId() != srcRace.getId()).toList();
			}
			return Collections.emptyList();
		}, result -> {
			this.srcRaceProp.clear();
			this.srcRaceProp.addAll((Collection<? extends Race>) result);
			if (this.srcRaceProp.size() == 1) {
				this.srcRaceCbo.getSelectionModel().selectFirst();
			}
			updateControls();
		});
	}

	@FXML
	private void handleSourceOfferOnAction() {
		Race srcRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();
		this.srcRaceVBox.getChildren().clear();

		if (srcRace != null) {
			showSrcRace();
			showRace();
			updateControls();
		}
	}

	@FXML
	private void handleRefreshOnAction() {
		Race race = this.raceCbo.getSelectionModel().getSelectedItem();
		if (race != null) {
			this.dbTask.run(() -> {
				Race raceTmp = this.regattaDAO.getRace(race.getNumber());
				this.db.getEntityManager().refresh(raceTmp);
				return null;
			});
		}

		Race srcRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();
		if (srcRace != null) {
			this.dbTask.run(() -> {
				Race raceTmp = this.regattaDAO.getRace(srcRace.getNumber());
				this.db.getEntityManager().refresh(raceTmp);
				return null;
			});
		}

		handleSourceOfferOnAction();
	}

	@FXML
	private void handleSetRaceOnAction() {
		Race race = this.raceCbo.getSelectionModel().getSelectedItem();
		Race sourceRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();

		if (race != null && sourceRace != null) {
			this.dbTask.runInTransaction(() -> {
				this.regattaDAO.setRaceHeats(race, sourceRace);
				return null;
			}, result -> showRace());
		}
	}

	@FXML
	private void handleDeleteOnAction() {
		Race race = this.raceCbo.getSelectionModel().getSelectedItem();
		if (race != null) {
			this.dbTask.runInTransaction(() -> {
				Race raceTmp = this.regattaDAO.getRace(race.getNumber());
				this.regattaDAO.cleanRaceHeats(raceTmp);
				return null;
			}, result -> showRace());
		}
	}

	// JavaFX stuff

	private void showRace(Race race, VBox vbox, boolean withResult) {
		vbox.getChildren().clear();
		Label title = new Label();
		vbox.getChildren().add(title);

		this.dbTask.run(() -> {
			race.getHeats().forEach(heat -> {
				List<HeatRegistration> entries = heat.getEntries();
				entries.forEach(entry -> {
					entry.getResults();
					entry.getRegistration().getClub().getAbbreviation();
					entry.getFinalResult();
				});
				SortedList<HeatRegistration> sortedList = new SortedList<>(FXCollections.observableArrayList(entries));

				Platform.runLater(() -> {
					Label heatNrLabel = new Label(getText("SetRaceView.heatNrLabel.text", heat.getHeatNumber()));
					TableView<HeatRegistration> compEntriesTable = createTableView(withResult);
					compEntriesTable.setItems(sortedList);
					sortedList.comparatorProperty().bind(compEntriesTable.comparatorProperty());

					vbox.getChildren().addAll(heatNrLabel, compEntriesTable);
				});
			});
			return new RaceStringConverter().toString(race);
		}, label -> title.setText(label));
	}

	private void showRace() {
		Race race = this.raceCbo.getSelectionModel().getSelectedItem();
		if (race != null) {
			this.dbTask.run(() -> this.regattaDAO.getRace(race.getNumber()),
					raceTmp -> showRace(raceTmp, this.raceVBox, false));
		}
	}

	private void showSrcRace() {
		Race srcRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();
		if (srcRace != null) {
			this.dbTask.run(() -> this.regattaDAO.getRace(srcRace.getNumber()),
					race -> showRace(race, this.srcRaceVBox, true));
		}
	}

	private void updateControls() {
		Race race = this.raceCbo.getSelectionModel().getSelectedItem();
		Race srcRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();

		this.srcRaceCbo.setDisable(race == null);
		boolean disabled = !(race != null && srcRace != null);
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

}
