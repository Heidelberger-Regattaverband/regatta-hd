package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.Offer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class SetRaceController extends AbstractBaseController {

	@FXML
	private ComboBox<Event> eventCombo;

	@FXML
	private ComboBox<Offer> sourceOfferCombo;

	@FXML
	private ComboBox<Offer> targetOfferCombo;

	@FXML
	private Button setRaceButton;

	@Inject
	private EventDAO eventsDAO;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.eventCombo.setItems(getEvents());

		this.targetOfferCombo.setItems(getTargetOffers());

		this.sourceOfferCombo.setItems(getSourceOffers());

		this.setRaceButton.setDisable(true);
	}

	@FXML
	private void handleEventOnAction() {
		this.targetOfferCombo.setItems(getTargetOffers());
	}

	@FXML
	private void handleTargetOfferOnAction() {
		ObservableList<Offer> offers = getSourceOffers();
		this.sourceOfferCombo.setItems(offers);
		if (offers.size() == 1) {
			this.sourceOfferCombo.getSelectionModel().selectFirst();
		}
	}

	@FXML
	private void handleSourceOfferOnAction() {
		this.setRaceButton.setDisable(false);
	}

	@FXML
	private void handleSetRaceOnAction() {
		this.eventsDAO.setRace(this.targetOfferCombo.getSelectionModel().getSelectedItem(),
				this.sourceOfferCombo.getSelectionModel().getSelectedItem());
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
