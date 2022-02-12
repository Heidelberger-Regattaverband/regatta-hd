package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.ui.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class RegattasController extends AbstractBaseController {
	private static final Logger logger = Logger.getLogger(RegattasController.class.getName());

	@FXML
	private Button refreshBtn;

	@FXML
	private TableView<Regatta> regattasTable;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		loadRegattas(false);
	}

	@FXML
	private void selectRegatta() throws IOException {
		Regatta regatta = this.regattasTable.getSelectionModel().getSelectedItem();
		this.regattaDAO.setActiveRegatta(regatta);
	}

	@FXML
	public void refresh() {
		loadRegattas(true);
	}

	private void loadRegattas(boolean refresh) {
		disableButtons(true);
		this.regattasTable.getItems().clear();

		this.dbTask.run(() -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getRegattas();
		}, dbResult -> {
			try {
				ObservableList<Regatta> regattas = FXCollections.observableArrayList(dbResult.getResult());
				this.regattasTable.setItems(regattas);
				FxUtils.autoResizeColumns(this.regattasTable);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
	}

}
