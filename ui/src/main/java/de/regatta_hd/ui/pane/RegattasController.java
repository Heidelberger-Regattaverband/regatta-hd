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

public class RegattasController extends AbstractRegattaController {
	private static final Logger logger = Logger.getLogger(RegattasController.class.getName());

	@FXML
	private Button refreshBtn;

	@FXML
	private TableView<Regatta> regattasTbl;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		loadRegattas(false);
	}

	@FXML
	public void handleRefreshOnAction() {
		loadRegattas(true);
	}

	@FXML
	private void handleSelectRegattaOnAction() throws IOException {
		Regatta regatta = this.regattasTbl.getSelectionModel().getSelectedItem();
		this.regattaDAO.setActiveRegatta(regatta);
	}

	private void loadRegattas(boolean refresh) {
		disableButtons(true);
		this.regattasTbl.getItems().clear();

		this.dbTask.run(() -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getRegattas();
		}, dbResult -> {
			try {
				ObservableList<Regatta> regattas = FXCollections.observableArrayList(dbResult.getResult());
				this.regattasTbl.setItems(regattas);
				FxUtils.autoResizeColumns(this.regattasTbl);
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
