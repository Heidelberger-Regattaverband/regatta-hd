package de.regatta_hd.ui.pane;

import java.net.URL;
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
import de.regatta_hd.ui.util.RaceStringConverter;
import de.regatta_hd.ui.util.TaskUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

	private final SimpleListProperty<Race> srcRaceProp = new SimpleListProperty<>();
	@FXML
	private FilterComboBox<Race> raceCbo;
	@FXML
	private ComboBox<Race> srcRaceCbo;
	@FXML
	private VBox sourceVBox;
	@FXML
	private VBox targetVBox;
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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.raceCbo.setDisable(true);
		updateControls();

		this.srcRaceCbo.itemsProperty().bind(this.srcRaceProp);

		TaskUtils.createAndRunTask(() -> {
			this.raceCbo.setInitialItems(getRaces());
			this.raceCbo.setDisable(false);
			updateControls();
			return Void.TYPE;
		});
	}

	@FXML
	private void handleTargetOfferOnAction() {
		TaskUtils.createAndRunTask(() -> {
			ObservableList<Race> srcRaces = getSourceRaces();

			Platform.runLater(() -> {
				this.srcRaceProp.set(srcRaces);
				if (srcRaces.size() == 1) {
					this.srcRaceCbo.getSelectionModel().selectFirst();
				}
				updateControls();
			});
			return Void.TYPE;
		});
	}

	@FXML
	private void handleSourceOfferOnAction() {
		Race srcRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();
		if (srcRace != null) {
			TaskUtils.createAndRunTask(() -> {
				Race race = this.regattaDAO.getRace(srcRace.getNumber());
				showRace(race, this.sourceVBox, true);
				return Void.TYPE;
			});
		}

		Race targetRace = this.raceCbo.getSelectionModel().getSelectedItem();
		if (targetRace != null) {
			TaskUtils.createAndRunTask(() -> {
				Race race = this.regattaDAO.getRace(targetRace.getNumber());
				showRace(race, this.targetVBox, false);
				return Void.TYPE;
			});
		}

		updateControls();
	}

	@FXML
	private void handleRefreshOnAction() {
		Race race = this.raceCbo.getSelectionModel().getSelectedItem();
		if (race != null) {
			race = this.regattaDAO.getRace(race.getNumber());
			this.db.getEntityManager().refresh(race);
		}

		Race srcRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();
		if (srcRace != null) {
			srcRace = this.regattaDAO.getRace(srcRace.getNumber());
			this.db.getEntityManager().refresh(srcRace);
		}

		handleSourceOfferOnAction();
	}

	@FXML
	private void handleSetRaceOnAction() {
		Race race = this.raceCbo.getSelectionModel().getSelectedItem();
		Race sourceRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();

		if (race != null && sourceRace != null) {
			this.regattaDAO.setRaceHeats(race, sourceRace);
			handleRefreshOnAction();
		}
	}

	@FXML
	private void handleDeleteOnAction() {
		Race race = this.raceCbo.getSelectionModel().getSelectedItem();
		if (race != null) {
			race = this.regattaDAO.getRace(race.getNumber());
			this.regattaDAO.cleanRaceHeats(race);
			handleRefreshOnAction();
		}
	}

	private void showRace(Race race, VBox vbox, boolean withResult) {
		Platform.runLater(() -> {
			vbox.getChildren().clear();
			Label title = new Label();
			title.setText(new RaceStringConverter().toString(race));
			vbox.getChildren().add(title);
		});

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

	private ObservableList<Race> getRaces() {
		List<Race> races = this.regattaDAO.findRaces("2%");

		// remove master races as they will not be set
		List<Race> filteredRaces = races.stream().filter(race -> !race.getAgeClass().isMasters()).toList();

		return FXCollections.observableArrayList(filteredRaces);
	}

	private ObservableList<Race> getSourceRaces() {
		Race race = this.raceCbo.getSelectionModel().getSelectedItem();

		if (race != null) {
			// get all races with same attributes
			List<Race> srcRaces = this.regattaDAO.findRaces("1%", race.getBoatClass(), race.getAgeClass(),
					race.isLightweight());

			srcRaces = srcRaces.stream().filter(srcRace -> race.getId() != srcRace.getId()).toList();

			return FXCollections.observableArrayList(srcRaces);
		}
		return FXCollections.emptyObservableList();
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
}
