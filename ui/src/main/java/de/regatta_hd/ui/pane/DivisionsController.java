package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.Event;
import de.regatta_hd.aquarius.db.model.Offer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

public class DivisionsController extends AbstractBaseController {
	@FXML
	private ComboBox<Event> eventCombo;

	@FXML
	private ComboBox<Offer> offerCombo;

	@Inject
	private EventDAO events;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		// add your data to the table here.
		this.eventCombo.setItems(getEvents());
		this.eventCombo.setConverter(new EventStringConverter());
		this.eventCombo.setOnAction(event -> {
			this.offerCombo.setItems(getOffers());
		});

		this.offerCombo.setItems(getOffers());
		this.offerCombo.setConverter(new OfferStringConverter());
	}

	// add your data here from any source
	private ObservableList<Event> getEvents() {
		return FXCollections.observableArrayList(this.events.getEvents());
	}

	// add your data here from any source
	private ObservableList<Offer> getOffers() {
		Event event = this.eventCombo.getSelectionModel().getSelectedItem();
		if (event != null) {
			return FXCollections.observableArrayList(event.getOffers());
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
