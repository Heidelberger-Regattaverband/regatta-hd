package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.ui.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class RegattasController extends AbstractBaseController {
	private static final Logger logger = Logger.getLogger(RegattasController.class.getName());

	@FXML
	private TableView<Regatta> regattasTable;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.dbTask.run(() -> this.regattaDAO.getRegattas(), dbResult -> {
			try {
				this.regattasTable.setItems(FXCollections.observableArrayList(dbResult.getResult()));
				FxUtils.autoResizeColumns(this.regattasTable);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			}
		});
	}

	@FXML
	private void selectRegatta() throws IOException {
		Regatta regatta = this.regattasTable.getSelectionModel().getSelectedItem();
		this.regattaDAO.setActiveRegatta(regatta);
	}
}
