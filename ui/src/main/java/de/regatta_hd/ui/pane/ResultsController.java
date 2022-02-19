package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.ui.util.FxUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class ResultsController extends AbstractRegattaController {
	private static final Logger logger = Logger.getLogger(ResultsController.class.getName());

	@FXML
	private Button refreshBtn;

	@FXML
	private TableView<HeatRegistration> resultsTbl;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		loadResults(false);
	}

	private void loadResults(boolean refresh) {
		disableButtons(true);
		this.resultsTbl.getItems().clear();

		this.dbTask.run(() -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getRegattas();
		}, dbResult -> {
			try {
//				ObservableList<Regatta> regattas = FXCollections.observableArrayList(dbResult.getResult());
//				this.resultsTbl.setItems(regattas);
//				FxUtils.autoResizeColumns(this.resultsTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});

	}

	@FXML
	public void handleRefreshOnAction() {
		disableButtons(true);

		disableButtons(false);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
	}

}
