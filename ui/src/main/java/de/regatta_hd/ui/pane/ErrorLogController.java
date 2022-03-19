package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

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
import javafx.scene.control.TextField;

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
	@FXML
	private TextField throwableTxf;

	@Inject
	private MasterDataDAO dao;
	@Inject
	@Named("hostName")
	private String hostName;

	private final ObservableList<LogRecord> logRecordsList = FXCollections.observableArrayList();
	private final ObservableList<String> hostNamesList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.logRecordsTbl.setItems(this.logRecordsList);
		this.hostNameCbx.setItems(this.hostNamesList);

		this.logRecordsTbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				String stackTraceTxt = newSelection.getStackTrace() != null ? newSelection.getStackTrace()
						: newSelection.getMessage();
				this.stackTraceTar.setText(stackTraceTxt);
				this.throwableTxf.setText(newSelection.getThrowable());
			} else {
				this.stackTraceTar.setText(null);
				this.throwableTxf.setText(null);
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
		this.hostNamesList.clear();

		super.dbTaskRunner.run(progress -> this.dao.getHostNames(), dbResult -> {
			try {
				this.hostNamesList.setAll(dbResult.getResult());
				this.hostNameCbx.getSelectionModel().select(this.hostName);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void loadLogRecords(String hostName, boolean refresh) {
		disableButtons(true);
		this.logRecordsList.clear();

		super.dbTaskRunner.run(progress -> {
			EntityManager entityManager = super.db.getEntityManager();
			if (refresh) {
				entityManager.clear();
			}
			return this.dao.getLogRecords(hostName);
		}, dbResult -> {
			try {
				this.logRecordsList.setAll(dbResult.getResult());
				FxUtils.autoResizeColumns(this.logRecordsTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
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
