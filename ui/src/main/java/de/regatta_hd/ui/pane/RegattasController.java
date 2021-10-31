package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.RegattaDAO;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.ui.util.DBTask;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class RegattasController extends AbstractBaseController {
	@FXML
	private TableView<Regatta> regattasTable;
	@Inject
	private RegattaDAO regattaDAO;
	@Inject
	private DBTask dbTask;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.dbTask.run(() -> this.regattaDAO.getRegattas(), regattas -> {
			this.regattasTable.setItems(FXCollections.observableArrayList(regattas));
		});
	}

	@FXML
	private void selectRegatta() throws IOException {
		Regatta regatta = this.regattasTable.getSelectionModel().getSelectedItem();
		this.regattaDAO.setActiveRegatta(regatta);
	}
}
