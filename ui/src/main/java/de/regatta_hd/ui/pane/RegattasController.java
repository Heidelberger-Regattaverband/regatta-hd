package de.regatta_hd.ui.pane;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Regatta;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class RegattasController extends AbstractBaseController {
	@FXML
	private TableView<Regatta> regattasTable;

	@FXML
	public TableColumn<Regatta, String> titleCol;

	@FXML
	public TableColumn<Regatta, Date> beginCol;

	@FXML
	public TableColumn<Regatta, String> endCol;

	@Inject
	private RegattaDAO regattaDAO;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
		this.beginCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
		this.endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

		// add your data to the table here.
		this.regattasTable.setItems(getEvents());
	}

	// add your data here from any source
	private ObservableList<Regatta> getEvents() {
		return FXCollections.observableArrayList(this.regattaDAO.getRegattas());
	}

	@FXML
	private void selectRegatta() {
		Regatta regatta = this.regattasTable.getSelectionModel().getSelectedItem();
		this.regattaDAO.setActiveRegatta(regatta);
	}
}
