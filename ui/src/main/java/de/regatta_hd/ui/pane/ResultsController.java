package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.ResultEntry;
import de.regatta_hd.ui.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ResultsController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(ResultsController.class.getName());

	@FXML
	private Button refreshBtn;
	@FXML
	private TableView<ResultEntry> resultsTbl;
	@FXML
	private TableColumn<ResultEntry, String> numberCol;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.resultsTbl.getSortOrder().add(this.numberCol);

		loadResults(false);
	}

	private void loadResults(boolean refresh) {
		disableButtons(true);

		this.dbTask.run(() -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getOfficialResults();
		}, dbResult -> {
			try {
				ObservableList<ResultEntry> results = FXCollections.observableArrayList(dbResult.getResult());
				this.resultsTbl.setItems(results);
				this.resultsTbl.getSortOrder().addAll(this.numberCol);

				FxUtils.autoResizeColumns(this.resultsTbl);
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

		loadResults(true);

		disableButtons(false);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
	}

}
