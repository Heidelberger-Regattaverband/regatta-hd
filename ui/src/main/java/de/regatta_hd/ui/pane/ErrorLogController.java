package de.regatta_hd.ui.pane;

import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.MasterDataDAO;
import de.regatta_hd.aquarius.model.LogRecord;
import de.regatta_hd.ui.util.FxUtils;
import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class ErrorLogController extends AbstractBaseController {
	private static final Logger logger = Logger.getLogger(ErrorLogController.class.getName());

	@FXML
	private Button refreshBtn;

	@FXML
	private TableView<LogRecord> logRecordsTbl;

	@Inject
	private MasterDataDAO dao;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		loadRegattas(false);
	}

	@FXML
	public void handleRefreshOnAction() {
		loadRegattas(true);
	}

	private void loadRegattas(boolean refresh) {
		disableButtons(true);
		this.logRecordsTbl.getItems().clear();

		this.dbTask.run(() -> {
			EntityManager entityManager = super.db.getEntityManager();
			if (refresh) {
				entityManager.clear();
			}
			return this.dao.getLogRecords(InetAddress.getLocalHost().getHostName());
		}, dbResult -> {
			try {
				ObservableList<LogRecord> regattas = FXCollections.observableArrayList(dbResult.getResult());
				this.logRecordsTbl.setItems(regattas);
				FxUtils.autoResizeColumns(this.logRecordsTbl);
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
