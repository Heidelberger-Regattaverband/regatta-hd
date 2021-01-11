package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.Offer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

public class DivisionsController extends AbstractBaseController {
	@FXML
	private ComboBox<Event> eventCombo;

	@FXML
	private ComboBox<Offer> sourceOfferCombo;

	@FXML
	private ComboBox<Offer> targetOfferCombo;

	@FXML
	private Button setRaceButton;

	@Inject
	private EventDAO events;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.eventCombo.setItems(getEvents());
		this.eventCombo.setConverter(new EventStringConverter());
		this.eventCombo.setOnAction(event -> {
			this.targetOfferCombo.setItems(getTargetOffers());
		});

		this.targetOfferCombo.setItems(getTargetOffers());
		this.targetOfferCombo.setConverter(new OfferStringConverter());
		this.targetOfferCombo.setOnAction(event -> {
			this.sourceOfferCombo.setItems(getSourceOffers());
		});

		this.sourceOfferCombo.setItems(getSourceOffers());
		this.sourceOfferCombo.setConverter(new OfferStringConverter());
		this.sourceOfferCombo.setOnAction(event -> {
			this.setRaceButton.setDisable(false);
		});

		this.setRaceButton.setDisable(true);
		this.setRaceButton.setOnAction(event -> {

		});
	}

	private ObservableList<Event> getEvents() {
		return FXCollections.observableArrayList(this.events.getEvents());
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
			List<Offer> sourceOffers = this.events.findOffers(event, targetOffer.getBoatClass(),
					targetOffer.getAgeClass(), targetOffer.isLightweight());
			return FXCollections.observableArrayList(sourceOffers);
		}
		return FXCollections.emptyObservableList();
	}

	class EventStringConverter extends StringConverter<Event> {

		@Override
		public String toString(Event event) {
			return event.getTitle();
		}

		@Override
		public Event fromString(String string) {
			return null;
		}
	}

	class OfferStringConverter extends StringConverter<Offer> {

		@Override
		public String toString(Offer offer) {
			return offer.getRaceNumber() + " - " + offer.getLongLabel();
		}

		@Override
		public Offer fromString(String string) {
			return null;
		}
	}
}
