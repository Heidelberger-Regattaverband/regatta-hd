package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.Entry;
import de.regatta_hd.aquarius.db.model.HeatEntry;
import de.regatta_hd.aquarius.db.model.Offer;
import de.regatta_hd.aquarius.db.model.Regatta;
import de.regatta_hd.aquarius.db.model.Result;
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

	private final SimpleListProperty<Regatta> eventsProp = new SimpleListProperty<>();

	private final SimpleListProperty<Offer> targetOffersProp = new SimpleListProperty<>();

	private final SimpleListProperty<Offer> sourceOffersProp = new SimpleListProperty<>();

	@FXML
	private ComboBox<Regatta> eventCombo;

	@FXML
	private ComboBox<Offer> sourceOfferCombo;

	@FXML
	private ComboBox<Offer> targetOfferCombo;

	@FXML
	private VBox sourceVBox;

	@FXML
	private VBox targetVBox;

	@FXML
	private Button setRaceButton;

	@Inject
	private EventDAO eventsDAO;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.eventsProp.set(getEvents());

		this.eventCombo.itemsProperty().bind(this.eventsProp);

		this.targetOfferCombo.itemsProperty().bind(this.targetOffersProp);

		this.sourceOfferCombo.itemsProperty().bind(this.sourceOffersProp);

		this.setRaceButton.setDisable(true);
	}

	@FXML
	private void handleEventOnAction() {
		this.targetOffersProp.set(getTargetOffers());
	}

	@FXML
	private void handleTargetOfferOnAction() {
		ObservableList<Offer> offers = getSourceOffers();
		this.sourceOffersProp.set(offers);
		if (offers.size() == 1) {
			this.sourceOfferCombo.getSelectionModel().selectFirst();
		}
	}

	@FXML
	private void handleSourceOfferOnAction() {
		this.setRaceButton.setDisable(false);

		Offer sourceOffer = this.sourceOfferCombo.getSelectionModel().getSelectedItem();
		showSourceOfferRaces(sourceOffer, this.sourceVBox, true);
		Offer targetOffer = this.targetOfferCombo.getSelectionModel().getSelectedItem();
		showSourceOfferRaces(targetOffer, this.targetVBox, false);
	}

	@FXML
	private void handleSetRaceOnAction() {
		this.eventsDAO.setRace(this.targetOfferCombo.getSelectionModel().getSelectedItem(),
				this.sourceOfferCombo.getSelectionModel().getSelectedItem());
	}

	private void showSourceOfferRaces(Offer offer, VBox vbox, boolean withResult) {
		vbox.getChildren().clear();

		Label title = new Label();
		title.setText(new OfferStringConverter().toString(offer));
		vbox.getChildren().add(title);

		if (offer != null) {
			offer.getHeats().forEach(heat -> {
				Label heatNrLabel = new Label(getText("SetRaceView.HeatNumberLabel.text", heat.getHeatNumber()));

				TableView<HeatEntry> compEntriesTable = createTableView(withResult);
				SortedList<HeatEntry> sortedList = new SortedList<>(
						FXCollections.observableArrayList(heat.getEntries()));
				compEntriesTable.setItems(sortedList);
				sortedList.comparatorProperty().bind(compEntriesTable.comparatorProperty());

				vbox.getChildren().addAll(heatNrLabel, compEntriesTable);
			});
		}
	}

	private TableView<HeatEntry> createTableView(boolean withResult) {
		TableView<HeatEntry> compEntriesTable = new TableView<>();

		TableColumn<HeatEntry, Number> bibCol = new TableColumn<>(
				getText("SetRaceView.CompEntriesTable.BibColumn.text"));
		bibCol.setStyle("-fx-alignment: CENTER;");
		bibCol.setCellValueFactory(row -> {
			Entry entry = row.getValue().getEntry();
			if (entry != null && entry.getBib() != null) {
				return new SimpleIntegerProperty(entry.getBib().shortValue());
			}
			return null;
		});

		TableColumn<HeatEntry, String> boatCol = new TableColumn<>(
				getText("SetRaceView.CompEntriesTable.BoatColumn.text"));
		boatCol.setCellValueFactory(row -> {
			Entry entry = row.getValue().getEntry();
			if (entry != null && entry.getClub() != null) {
				String value = entry.getClub().getAbbr();
				if (entry.getBoatNumber() != null) {
					value += " - Boot " + entry.getBoatNumber();
				}
				return new SimpleStringProperty(value);
			}
			return null;
		});

		TableColumn<HeatEntry, Number> rankCol = null;
		TableColumn<HeatEntry, String> resultCol = null;
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

	private ObservableList<Regatta> getEvents() {
		return FXCollections.observableArrayList(this.eventsDAO.getEvents());
	}

	private ObservableList<Offer> getTargetOffers() {
		Regatta event = this.eventCombo.getSelectionModel().getSelectedItem();
		if (event != null) {
			List<Offer> offers = this.eventsDAO.findOffers(event, "2%");
			return FXCollections.observableArrayList(offers);
		}
		return FXCollections.emptyObservableList();
	}

	private ObservableList<Offer> getSourceOffers() {
		Regatta event = this.eventCombo.getSelectionModel().getSelectedItem();
		Offer targetOffer = this.targetOfferCombo.getSelectionModel().getSelectedItem();

		if (event != null && targetOffer != null) {
			// get all offers with same attributes
			List<Offer> sourceOffers = this.eventsDAO.findOffers(event, targetOffer.getBoatClass(),
					targetOffer.getAgeClass(), targetOffer.isLightweight());

			// filter target offer
			sourceOffers = sourceOffers.stream().filter(offer -> targetOffer.getId() != offer.getId())
					.collect(Collectors.toList());

			return FXCollections.observableArrayList(sourceOffers);
		}
		return FXCollections.emptyObservableList();
	}
}
