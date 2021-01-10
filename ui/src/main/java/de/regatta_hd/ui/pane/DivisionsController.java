package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class DivisionsController extends AbstractBaseController {
	@FXML
	private ComboBox<Event> eventCombo;

	@Inject
	private EventDAO events;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		// add your data to the table here.
		this.eventCombo.setItems(getEvents());
	}

	// add your data here from any source
	private ObservableList<Event> getEvents() {
		return FXCollections.observableArrayList(this.events.getEvents());
	}

}
