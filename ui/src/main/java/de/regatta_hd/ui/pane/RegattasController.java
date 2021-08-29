package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Regatta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class RegattasController extends AbstractBaseController {
	@FXML
	private TableView<Regatta> regattasTable;

	@Inject
	private RegattaDAO regattaDAO;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

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
