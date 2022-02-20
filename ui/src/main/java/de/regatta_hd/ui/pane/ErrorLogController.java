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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

public class ErrorLogController extends AbstractBaseController {
	private static final Logger logger = Logger.getLogger(ErrorLogController.class.getName());

	@FXML
	private Button refreshBtn;
	@FXML
	private TableView<LogRecord> logRecordsTbl;
	@FXML
	private ComboBox<String> hostNameCbx;
	@FXML
	private TextArea stackTraceTar;

	@Inject
	private MasterDataDAO dao;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.logRecordsTbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				this.stackTraceTar.setText(newSelection.getStackTrace());
			} else {
				this.stackTraceTar.setText(null);
			}
		});

		loadHostNames();
	}

	@FXML
	public void handleRefreshOnAction() {
		loadLogRecords(this.hostNameCbx.getSelectionModel().getSelectedItem(), true);
	}

	@FXML
	public void handleHostNameOnAction() {
		loadLogRecords(this.hostNameCbx.getSelectionModel().getSelectedItem(), false);
	}

	private void loadHostNames() {
		disableButtons(true);
		this.hostNameCbx.getItems().clear();

		this.dbTask.run(() -> this.dao.getHostNames(), dbResult -> {
			try {
				ObservableList<String> regattas = FXCollections.observableArrayList(dbResult.getResult());
				this.hostNameCbx.setItems(regattas);
				this.hostNameCbx.getSelectionModel().select(InetAddress.getLocalHost().getHostName());
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void loadLogRecords(String hostName, boolean refresh) {
		disableButtons(true);
		this.logRecordsTbl.getItems().clear();

		this.dbTask.run(() -> {
			EntityManager entityManager = super.db.getEntityManager();
			if (refresh) {
				entityManager.clear();
			}
			return this.dao.getLogRecords(hostName);
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
		this.hostNameCbx.setDisable(disabled);
	}
}
