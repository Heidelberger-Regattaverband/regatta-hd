package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.db.RegattaDAO;
import de.regatta_hd.aquarius.db.model.HeatRegistration;
import de.regatta_hd.aquarius.db.model.Offer;
import de.regatta_hd.aquarius.db.model.Regatta;
import de.regatta_hd.aquarius.db.model.Registration;
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
	private ComboBox<Regatta> regattaCombo;

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
	private RegattaDAO regattaDAO;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.eventsProp.set(getRegattas());

		this.regattaCombo.itemsProperty().bind(this.eventsProp);

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
		this.regattaDAO.setRace(this.targetOfferCombo.getSelectionModel().getSelectedItem(),
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

	private ObservableList<Regatta> getRegattas() {
		return FXCollections.observableArrayList(this.regattaDAO.getRegattas());
	}

	private ObservableList<Offer> getTargetOffers() {
		Regatta regatta = this.regattaCombo.getSelectionModel().getSelectedItem();
		this.regattaDAO.setActiveRegatta(regatta);

		if (regatta != null) {
			List<Offer> offers = this.regattaDAO.findOffers("2%");
			return FXCollections.observableArrayList(offers);
		}
		return FXCollections.emptyObservableList();
	}

	private ObservableList<Offer> getSourceOffers() {
		Regatta event = this.regattaCombo.getSelectionModel().getSelectedItem();
		Offer targetOffer = this.targetOfferCombo.getSelectionModel().getSelectedItem();

		if (event != null && targetOffer != null) {
			// get all offers with same attributes
			List<Offer> sourceOffers = this.regattaDAO.findOffers(targetOffer.getBoatClass(),
					targetOffer.getAgeClass(), targetOffer.isLightweight());

			// filter target offer
			sourceOffers = sourceOffers.stream().filter(offer -> targetOffer.getId() != offer.getId())
					.collect(Collectors.toList());

			return FXCollections.observableArrayList(sourceOffers);
		}
		return FXCollections.emptyObservableList();
	}
}
