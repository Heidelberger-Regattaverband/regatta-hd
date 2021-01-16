package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.CompEntries;
import de.regatta_hd.aquarius.db.model.Entry;
import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.Offer;
import de.regatta_hd.aquarius.db.model.Result;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class SetRaceController extends AbstractBaseController {

	private final SimpleListProperty<Event> eventsProp = new SimpleListProperty<>();

	private final SimpleListProperty<Offer> targetOffersProp = new SimpleListProperty<>();

	private final SimpleListProperty<Offer> sourceOffersProp = new SimpleListProperty<>();

	@FXML
	private ComboBox<Event> eventCombo;

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

		showSourceOfferRaces();
	}

	@FXML
	private void handleSetRaceOnAction() {
		this.eventsDAO.setRace(this.targetOfferCombo.getSelectionModel().getSelectedItem(),
				this.sourceOfferCombo.getSelectionModel().getSelectedItem());
	}

	private void showSourceOfferRaces() {
		this.sourceVBox.getChildren().clear();

		Offer sourceOffer = this.sourceOfferCombo.getSelectionModel().getSelectedItem();
		if (sourceOffer != null) {
			sourceOffer.getComps().forEach(comps -> {
				Label heatNrLabel = new Label();
				heatNrLabel.setText("Abteilung " + comps.getHeatNumber());

				TableView<CompEntries> compEntriesTable = createTableView();
				compEntriesTable.setItems(FXCollections.observableArrayList(comps.getCompEntries()));
				compEntriesTable.sort();

				this.sourceVBox.getChildren().addAll(heatNrLabel, compEntriesTable);
			});
		}
	}

	private TableView<CompEntries> createTableView() {
		TableView<CompEntries> compEntriesTable = new TableView<>();

		TableColumn<CompEntries, Number> startNrCol = new TableColumn<>("Startnr.");
		startNrCol.setStyle("-fx-alignment: CENTER;");
		startNrCol.setCellValueFactory(row -> {
			Entry entry = row.getValue().getEntry();
			if (entry != null && entry.getBib() != null) {
				return new SimpleIntegerProperty(entry.getBib().shortValue());
			}
			return null;
		});

		TableColumn<CompEntries, String> clubCol = new TableColumn<>("Boot");
		clubCol.setCellValueFactory(row -> {
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

		TableColumn<CompEntries, Number> rankCol = new TableColumn<>("Rang");
		rankCol.setSortType(SortType.ASCENDING);
		rankCol.setSortable(true);
		rankCol.setStyle("-fx-alignment: CENTER;");
		rankCol.setCellValueFactory(row -> {
			Set<Result> results = row.getValue().getResults();
			for (Result result : results) {
				if (result.getSplitNr() == 64) {
					return new SimpleIntegerProperty(result.getRank().byteValue());
				}
			}
			return null;
		});

		TableColumn<CompEntries, String> resultCol = new TableColumn<>("Ergebnis");
		resultCol.setStyle("-fx-alignment: CENTER_RIGHT;");
		resultCol.setCellValueFactory(row -> {
			Set<Result> results = row.getValue().getResults();
			for (Result result : results) {
				if (result.getSplitNr() == 64) {
					return new SimpleStringProperty(result.getDisplayValue());
				}
			}
			return null;
		});

		compEntriesTable.getColumns().add(rankCol);
		compEntriesTable.getColumns().add(startNrCol);
		compEntriesTable.getColumns().add(clubCol);
		compEntriesTable.getColumns().add(resultCol);

		compEntriesTable.getSortOrder().add(rankCol);

		return compEntriesTable;
	}

	private ObservableList<Event> getEvents() {
		return FXCollections.observableArrayList(this.eventsDAO.getEvents());
	}

	private ObservableList<Offer> getTargetOffers() {
		Event event = this.eventCombo.getSelectionModel().getSelectedItem();
		if (event != null) {
			return FXCollections.observableArrayList(event.getOffers());
		}
		return FXCollections.emptyObservableList();
	}

	private ObservableList<Offer> getSourceOffers() {
		Event event = this.eventCombo.getSelectionModel().getSelectedItem();
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
