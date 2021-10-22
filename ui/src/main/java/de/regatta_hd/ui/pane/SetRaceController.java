package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Race;
import de.regatta_hd.aquarius.model.Registration;
import de.regatta_hd.aquarius.model.Result;
import de.regatta_hd.ui.control.FilterComboBox;
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

	private final SimpleListProperty<Race> sourceOffersProp = new SimpleListProperty<>();
	@FXML
	private FilterComboBox<Race> targetRaceCbo;
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

		updateControls();

		this.srcRaceCbo.itemsProperty().bind(this.sourceOffersProp);

		TaskUtils.createAndRunTask(() -> {
			this.targetRaceCbo.setInitialItems(getTargetOffers());
			this.targetRaceCbo.setDisable(false);
			updateControls();
			return Void.TYPE;
		});
	}

	@FXML
	private void handleTargetOfferOnAction() {
		TaskUtils.createAndRunTask(() -> {
			ObservableList<Race> offers = getSourceOffers();

			Platform.runLater(() -> {
				this.sourceOffersProp.set(offers);
				if (offers.size() == 1) {
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
			srcRace = this.regattaDAO.getOffer(srcRace.getRaceNumber());
			showRace(srcRace, this.sourceVBox, true);
		}

		Race targetRace = this.targetRaceCbo.getSelectionModel().getSelectedItem();
		if (targetRace != null) {
			targetRace = this.regattaDAO.getOffer(targetRace.getRaceNumber());
			showRace(targetRace, this.targetVBox, false);
		}

		updateControls();
	}

	@FXML
	private void handleRefreshOnAction() {
		Race targetRace = this.targetRaceCbo.getSelectionModel().getSelectedItem();
		if (targetRace != null) {
			targetRace = this.regattaDAO.getOffer(targetRace.getRaceNumber());
			this.db.getEntityManager().refresh(targetRace);
		}

		Race srcRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();
		if (srcRace != null) {
			srcRace = this.regattaDAO.getOffer(srcRace.getRaceNumber());
			this.db.getEntityManager().refresh(srcRace);
		}

		handleSourceOfferOnAction();
	}

	@FXML
	private void handleSetRaceOnAction() {
		Race targetRace = this.targetRaceCbo.getSelectionModel().getSelectedItem();
		Race sourceRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();

		if (targetRace != null && sourceRace != null) {
			this.regattaDAO.assignRace(targetRace, sourceRace);
			handleRefreshOnAction();
		}
	}

	@FXML
	private void handleDeleteOnAction() {
		Race race = this.targetRaceCbo.getSelectionModel().getSelectedItem();
		race = this.regattaDAO.getOffer(race.getRaceNumber());

		if (race != null) {
			this.regattaDAO.deleteAssignment(race);
			handleRefreshOnAction();
		}
	}

	private void showRace(Race offer, VBox vbox, boolean withResult) {
		vbox.getChildren().clear();

		Label title = new Label();
		title.setText(new OfferStringConverter().toString(offer));
		vbox.getChildren().add(title);

		if (offer != null) {
			offer.getHeats().forEach(heat -> {
				Label heatNrLabel = new Label(getText("SetRaceView.HeatNumberLabel.text", heat.getHeatNumber()));

				TableView<HeatRegistration> compEntriesTable = createTableView(withResult);
				SortedList<HeatRegistration> sortedList = new SortedList<>(
						FXCollections.observableArrayList(heat.getEntries()));
				compEntriesTable.setItems(sortedList);
				sortedList.comparatorProperty().bind(compEntriesTable.comparatorProperty());

				vbox.getChildren().addAll(heatNrLabel, compEntriesTable);
			});
		}
	}

	private TableView<HeatRegistration> createTableView(boolean withResult) {
		TableView<HeatRegistration> compEntriesTable = new TableView<>();

		TableColumn<HeatRegistration, Number> bibCol = new TableColumn<>(
				getText("SetRaceView.CompEntriesTable.BibColumn.text"));
		bibCol.setStyle("-fx-alignment: CENTER;");
		bibCol.setCellValueFactory(row -> {
			Registration entry = row.getValue().getRegistration();
			if (entry != null && entry.getBib() != null) {
				return new SimpleIntegerProperty(entry.getBib().shortValue());
			}
			return null;
		});

		TableColumn<HeatRegistration, String> boatCol = new TableColumn<>(
				getText("SetRaceView.CompEntriesTable.BoatColumn.text"));
		boatCol.setCellValueFactory(row -> {
			Registration entry = row.getValue().getRegistration();
			if (entry != null && entry.getClub() != null) {
				String value = entry.getClub().getAbbr();
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
			rankCol = new TableColumn<>(getText("SetRaceView.CompEntriesTable.RankColumn.text"));
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

			resultCol = new TableColumn<>(getText("SetRaceView.CompEntriesTable.ResultColumn.text"));
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

			compEntriesTable.getColumns().add(rankCol);
		}

		if (rankCol != null) {
			compEntriesTable.getSortOrder().add(rankCol);
		}

		compEntriesTable.getColumns().add(bibCol);
		compEntriesTable.getColumns().add(boatCol);

		if (resultCol != null) {
			compEntriesTable.getColumns().add(resultCol);
		}

		return compEntriesTable;
	}

	private ObservableList<Race> getTargetOffers() {
		List<Race> offers = this.regattaDAO.findOffers("2%");

		// remove master races as they will not be set
		List<Race> filteredOffers = offers.stream().filter(offer -> {
			String abbrevation = offer.getAgeClass().getAbbreviation();
			return !StringUtils.equalsAny(abbrevation, "MM", "MW", "MM/W");
		}).toList();
		return FXCollections.observableArrayList(filteredOffers);
	}

	private ObservableList<Race> getSourceOffers() {
		Race targetOffer = this.targetRaceCbo.getSelectionModel().getSelectedItem();

		if (targetOffer != null) {
			// get all offers with same attributes
			List<Race> sourceOffers = this.regattaDAO.findOffers("1%", targetOffer.getBoatClass(),
					targetOffer.getAgeClass(), targetOffer.isLightweight());

			// filter target offer
			sourceOffers = sourceOffers.stream().filter(offer -> targetOffer.getId() != offer.getId()).toList();

			return FXCollections.observableArrayList(sourceOffers);
		}
		return FXCollections.emptyObservableList();
	}

	private void updateControls() {
		Race targetRace = this.targetRaceCbo.getSelectionModel().getSelectedItem();
		Race srcRace = this.srcRaceCbo.getSelectionModel().getSelectedItem();

		this.srcRaceCbo.setDisable(targetRace == null);
		boolean disabled = !(targetRace != null && srcRace != null);
		this.refreshBtn.setDisable(disabled);
		this.deleteBtn.setDisable(disabled);
		this.setRaceBtn.setDisable(disabled);
	}
}
