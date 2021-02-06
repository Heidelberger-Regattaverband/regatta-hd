package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.db.RegattaDAO;
import de.regatta_hd.aquarius.db.model.Regatta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class RegattasController extends AbstractBaseController {
	@FXML
	private TableView<Regatta> tbData;

	@FXML
	public TableColumn<Regatta, String> title;

	@FXML
	public TableColumn<Regatta, Date> begin;

	@FXML
	public TableColumn<Regatta, String> end;

	@Inject
	private RegattaDAO events;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		// make sure the property value factory should be exactly same as the e.g
		// getStudentId from your model class
		this.title.setCellValueFactory(new PropertyValueFactory<>("title"));
		this.begin.setCellValueFactory(new PropertyValueFactory<>("startDate"));
		this.end.setCellValueFactory(new PropertyValueFactory<>("endDate"));

		// add your data to the table here.
		this.tbData.setItems(getEvents());
	}

	// add your data here from any source
	private ObservableList<Regatta> getEvents() {
		return FXCollections.observableArrayList(this.events.getRegattas());
	}
}
