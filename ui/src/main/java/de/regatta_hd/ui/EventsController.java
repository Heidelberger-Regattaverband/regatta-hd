package de.regatta_hd.ui;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.EventDAO;
import de.regatta_hd.aquarius.db.model.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class EventsController implements Initializable {
	@FXML
	private TableView<Event> tbData;

	@FXML
	public TableColumn<Event, String> title;

	@FXML
	public TableColumn<Event, Date> begin;

	@FXML
	public TableColumn<Event, String> end;

	@Inject
	private EventDAO events;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// make sure the property value factory should be exactly same as the e.g
		// getStudentId from your model class
		this.title.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
		this.begin.setCellValueFactory(new PropertyValueFactory<>("eventStartDate"));
		this.end.setCellValueFactory(new PropertyValueFactory<>("eventEndDate"));

		// add your data to the table here.
		this.tbData.setItems(getEvents());
	}

	// add your data here from any source
	private ObservableList<Event> getEvents() {
		return FXCollections.observableArrayList(this.events.getEvents());
	}
}
