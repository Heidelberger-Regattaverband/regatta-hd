package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.MasterDataDAO;
import de.regatta_hd.aquarius.model.LogRecord;
import de.regatta_hd.commons.core.ListenerManager;
import de.regatta_hd.commons.db.DBConnection.StateChangedEventListener;
import de.regatta_hd.commons.fx.stage.PaneController;
import de.regatta_hd.commons.fx.util.FxUtils;
import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ErrorLogController extends PaneController {
	private static final Logger logger = Logger.getLogger(ErrorLogController.class.getName());

	@FXML
	private Button refreshBtn;
	@FXML
	private ComboBox<String> hostNameCbx;
	@FXML
	private Button deleteBtn;

	@FXML
	private TableView<LogRecord> logRecordsTbl;
	@FXML
	private TextArea stackTraceTar;
	@FXML
	private TextField throwableTxf;

	@Inject
	private ListenerManager listenerManager;
	@Inject
	private MasterDataDAO dao;
	@Inject
	@Named("hostName")
	private String hostName;

	private final ObservableList<LogRecord> logRecordsList = FXCollections.observableArrayList();
	private final ObservableList<String> hostNamesList = FXCollections.observableArrayList();

	private final StateChangedEventListener eventListener = event -> {
		disableButtons(!event.getDBConnection().isOpen());

		if (event.getDBConnection().isOpen()) {
			loadHostNames();
		} else {
			this.hostNamesList.clear();
			this.logRecordsList.clear();
		}
	};

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

		this.listenerManager.addListener(StateChangedEventListener.class, this.eventListener);

		loadHostNames();
	}

	@Override
	public void shutdown() {
		this.listenerManager.removeListener(StateChangedEventListener.class, this.eventListener);
	}

	@FXML
	void handleRefreshOnAction() {
		loadLogRecords(true);
	}

	@FXML
	void handleHostNameOnAction() {
		loadLogRecords(false);
	}

	@FXML
	void handleDeleteOnAction() {
		disableButtons(true);

		String selectedHostName = this.hostNameCbx.getSelectionModel().getSelectedItem();
		boolean delete = FxUtils.showConfirmDialog(getWindow(), getText("errorLog.confirmDelete.title"),
				getText("errorLog.confirmDelete.question", selectedHostName));

		if (delete) {
			super.dbTaskRunner.runInTransaction(progress -> {
				return Integer.valueOf(this.dao.deleteLogRecords(selectedHostName));
			}, dbResult -> {
				try {
					dbResult.getResult();
					loadHostNames();
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					disableButtons(false);
				}
			});
		} else {
			disableButtons(false);
		}
	}

	private void loadHostNames() {
		disableButtons(true);

		super.dbTaskRunner.run(progress -> this.dao.getHostNames(), dbResult -> {
			try {
				this.hostNamesList.setAll(dbResult.getResult());

				if (this.hostNamesList.contains(this.hostName)) {
					this.hostNameCbx.getSelectionModel().select(this.hostName);
				} else {
					this.hostNameCbx.getSelectionModel().selectFirst();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void loadLogRecords(boolean refresh) {
		String selectedHostName = this.hostNameCbx.getSelectionModel().getSelectedItem();

		if (selectedHostName != null) {
			disableButtons(true);
			updatePlaceholder(getText("common.loadData"));

			super.dbTaskRunner.run(progress -> {
				EntityManager entityManager = super.db.getEntityManager();
				if (refresh) {
					entityManager.clear();
				}
				return this.dao.getLogRecords(selectedHostName);
			}, dbResult -> {
				try {
					this.logRecordsList.setAll(dbResult.getResult());
					FxUtils.autoResizeColumns(this.logRecordsTbl);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					updatePlaceholder(getText("common.noDataAvailable"));
					disableButtons(false);
				}
			});
		}
	}

	private void updatePlaceholder(String text) {
		((Label) this.logRecordsTbl.getPlaceholder()).setText(text);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.hostNameCbx.setDisable(disabled);
		this.deleteBtn.setDisable(disabled);
	}

}
