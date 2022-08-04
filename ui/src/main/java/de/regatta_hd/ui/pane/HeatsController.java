package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Workbook;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.fx.db.DBTask;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.ui.UIModule;
import de.regatta_hd.ui.util.SerialPortUtils;
import de.regatta_hd.ui.util.TrafficLightsStartList;
import de.regatta_hd.ui.util.WorkbookUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;

public class HeatsController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(HeatsController.class.getName());

	// main toolbar
	@FXML
	private Button refreshBtn;
	@FXML
	private Button exportCsvBtn;
	@FXML
	private Button exportXslBtn;
	@FXML
	private ToggleButton startSignalTbtn;

	// heats table
	@FXML
	private TableView<Heat> heatsTbl;
	@FXML
	private TableColumn<Heat, Instant> timeCol;
	@FXML
	private TableColumn<Heat, Integer> heatsIdCol;
	@FXML
	private Menu stateMenu;

	// division table
	@FXML
	private TableView<HeatRegistration> divisionTbl;
	@FXML
	private TableColumn<HeatRegistration, Integer> divisionIdCol;
	@FXML
	private TableColumn<HeatRegistration, Short> divisionLaneCol;
	@FXML
	private Menu swapMenu;

	// injections
	@Inject
	private TrafficLightsStartList startList;
	@Inject
	@Named(UIModule.CONFIG_SHOW_ID_COLUMN)
	private BooleanProperty showIdColumn;
	@Inject
	@Named(UIModule.CONFIG_SERIAL_PORT_START_SIGNAL)
	private StringProperty serialPortStartSignal;

	// private fields
	private final ObservableList<Heat> heatsList = FXCollections.observableArrayList();
	private final ObservableList<HeatRegistration> divisionList = FXCollections.observableArrayList();
	private Optional<SerialPort> serialPortOpt;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.heatsIdCol.visibleProperty().bind(this.showIdColumn);
		this.divisionIdCol.visibleProperty().bind(this.showIdColumn);

		this.heatsTbl.setItems(this.heatsList);
		this.heatsTbl.getSortOrder().add(this.timeCol);
		this.heatsTbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				this.divisionList.setAll(newSelection.getEntries());
				this.divisionTbl.sort();
			} else {
				this.divisionList.clear();
			}
		});
		this.heatsTbl.setRowFactory(row -> new TableRow<>() {
			@Override
			public void updateItem(Heat item, boolean empty) {
				super.updateItem(item, empty);
				pseudoClassStateChanged(PseudoClass.getPseudoClass("highlighted"), item != null && item.isCancelled());
			}
		});

		this.divisionTbl.setItems(this.divisionList);
		this.divisionTbl.getSortOrder().add(this.divisionLaneCol);

		this.serialPortOpt = SerialPortUtils.getSerialPortByPath(this.serialPortStartSignal.get());
		this.startSignalTbtn.setDisable(this.serialPortOpt.isEmpty());

		loadHeats(false);
	}

	@Override
	public void shutdown() {
		closePort();
		// don't forget to shutdown parent class
		super.shutdown();
	}

	private void openPort() {
		if (this.serialPortOpt.isPresent()) {
			SerialPort serialPort = this.serialPortOpt.get();
			boolean openPort = serialPort.isOpen() || serialPort.openPort();

			if (openPort) {
				SerialPortDataListener serialPortListener = new SerialPortDataListener() {

					@Override
					public void serialEvent(SerialPortEvent event) {
						event.getEventType();
						System.out.println(Arrays.toString(event.getReceivedData()));
					}

					@Override
					public int getListeningEvents() {
						return SerialPort.LISTENING_EVENT_DATA_RECEIVED | SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
					}
				};
				serialPort.addDataListener(serialPortListener);

//			try (InputStream portIn = port.getInputStreamWithSuppressedTimeoutExceptions()) {
//				byte[] buffer = new byte[1024];
//				int read = portIn.read(buffer);
//				while (read >= 0) {
//					System.out.println("Read " + read + ": "+ Arrays.toString(buffer));
//					read = portIn.read(buffer);
//				}
//			} catch (IOException e) {
//				logger.log(Level.SEVERE, e.getMessage(), e);
//				FxUtils.showErrorMessage(getWindow(), e);
//			} finally {
//				port.closePort();
//			}
			} else {
				FxUtils.showErrorMessage(getWindow(), "Serial Port", "Not open.");
			}
		}
	}

	private void closePort() {
		if (this.serialPortOpt.isPresent()) {
			SerialPort serialPort = this.serialPortOpt.get();
			serialPort.removeDataListener();
			serialPort.closePort();
		}
	}

	@Override
	protected void onActiveRegattaChanged(Regatta activeRegatta) {
		if (activeRegatta != null) {
			loadHeats(true);
		} else {
			this.heatsList.clear();
			disableButtons(true);
		}
	}

	@FXML
	void handleRefreshOnAction() {
		loadHeats(true);
	}

	@FXML
	void handleExportCsvOnAction() {
		disableButtons(true);

		File file = FxUtils.showSaveDialog(getWindow(), "startliste.csv", getText("heats.csv.description"), "*.csv");
		if (file != null) {
			DBTask<String> dbTask = super.dbTaskRunner.createTask(progress -> {
				return this.startList.createCsv(this.heatsList, progress);
			}, dbResult -> {
				try {
					saveCsvFile(dbResult.getResult(), file);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					disableButtons(false);
				}
			}, false);

			runTaskWithProgressDialog(dbTask, getText("heats.csv.export"), false);
		} else {
			disableButtons(false);
		}
	}

	@FXML
	void handleExportXslOnAction() {
		disableButtons(true);

		File file = FxUtils.showSaveDialog(getWindow(), "startliste.xls", getText("heats.xsl.description"), "*.xls");
		if (file != null) {
			DBTask<Workbook> dbTask = super.dbTaskRunner.createTask(progress -> {
				return this.startList.createWorkbook(this.heatsList, progress);
			}, dbResult -> {
				try (Workbook workbook = dbResult.getResult()) {
					WorkbookUtils.saveWorkbook(workbook, file);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					disableButtons(false);
				}
			}, false);

			runTaskWithProgressDialog(dbTask, getText("heats.csv.export"), false);
		} else {
			disableButtons(false);
		}
	}

	@FXML
	void handleStartSignalOnAction() {
		if (this.startSignalTbtn.isSelected()) {
			openPort();
		} else {
			closePort();
		}
	}

	@FXML
	void handleHeatsContextMenuOnShowing() {
		this.stateMenu.getItems().clear();

		Heat selectedHeat = this.heatsTbl.getSelectionModel().getSelectedItem();
		if (selectedHeat != null) {
			int currentState = selectedHeat.getState();
			byte[] allowedStates = Heat.getAllowedStates();
			for (int i = 0; i < allowedStates.length; i++) {
				if (currentState != allowedStates[i]) {
					final byte newState = allowedStates[i];
					String newStateLabel = Heat.getStateLabel(allowedStates[i]);
					MenuItem menuItem = new MenuItem(newStateLabel);
					menuItem.addEventHandler(ActionEvent.ACTION, event -> {
						// confirm changing the state
						if (FxUtils.showConfirmDialog(getWindow(), getText("heats.confirmChangeState.title"), getText(
								"heats.confirmChangeState.question", selectedHeat.getStateLabel(), newStateLabel))) {
							// changing state requires a transaction
							this.dbTaskRunner.runInTransaction(progress -> {
								selectedHeat.setState(newState);
								return this.db.getEntityManager().merge(selectedHeat);
							}, dbResult -> {
								try {
									int indexOf = this.heatsList.indexOf(selectedHeat);
									this.heatsList.set(indexOf, dbResult.getResult());
								} catch (Exception ex) {
									logger.log(Level.SEVERE, ex.getMessage(), ex);
									FxUtils.showErrorMessage(getWindow(), ex);
								}
							});
						}
					});
					this.stateMenu.getItems().add(menuItem);
				}
			}
		}

		// disable context menu if it's empty
		this.stateMenu.setVisible(!this.stateMenu.getItems().isEmpty());
	}

	@FXML
	void handleDivisionContextMenuOnShowing() {
		this.swapMenu.getItems().clear();

		Heat selectedHeat = this.heatsTbl.getSelectionModel().getSelectedItem();
		if (selectedHeat != null && selectedHeat.isStateFinished()) {
			HeatRegistration selectedItem = this.divisionTbl.getSelectionModel().getSelectedItem();

			this.divisionList.stream().filter(heatReg -> heatReg.getId() != selectedItem.getId()).forEach(heatReg -> {
				MenuItem menuItem = new MenuItem(heatReg.getBib() + " - " + heatReg.getBoatLabel());
				menuItem.addEventHandler(ActionEvent.ACTION, event -> {
					// confirm swapping the results
					if (FxUtils.showConfirmDialog(getWindow(), getText("heats.confirmSwapRsult.title"), getText(
							"heats.confirmSwapRsult.question", selectedItem.getBoatLabel(), heatReg.getBoatLabel()))) {
						// swapping results requires a transaction
						this.dbTaskRunner.runInTransaction(progress -> {
							return this.regattaDAO.swapResults(heatReg, selectedItem);
						}, dbResult -> {
							try {
								this.divisionList.setAll(dbResult.getResult().getEntries());
								this.divisionTbl.sort();
							} catch (Exception ex) {
								logger.log(Level.SEVERE, ex.getMessage(), ex);
								FxUtils.showErrorMessage(getWindow(), ex);
							}
						});
					}
				});
				this.swapMenu.getItems().add(menuItem);
			});
		}

		// disable context menu if it's empty
		this.swapMenu.setVisible(!this.swapMenu.getItems().isEmpty());
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("heats.title") + " - " + activeRegatta.getTitle()
				: getText("heats.title");
	}

	private void loadHeats(boolean refresh) {
		disableButtons(true);
		updatePlaceholder(getText("common.loadData"));
		Heat selectedItem = this.heatsTbl.getSelectionModel().getSelectedItem();

		super.dbTaskRunner.run(progress -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getHeats(Heat.GRAPH_ENTRIES);
		}, dbResult -> {
			try {
				this.heatsList.setAll(dbResult.getResult());
				this.heatsTbl.sort();
				this.heatsTbl.getSelectionModel().select(selectedItem);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				updatePlaceholder(getText("common.noDataAvailable"));
				disableButtons(false);
			}
		});
	}

	private void saveCsvFile(String csvContent, File file) {
		try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
			writer.println(csvContent);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			FxUtils.showErrorMessage(getWindow(), ex);
		}
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.exportCsvBtn.setDisable(disabled);
		this.exportXslBtn.setDisable(disabled);
		this.heatsTbl.setDisable(disabled);
		this.divisionTbl.setDisable(disabled);
	}

	private void updatePlaceholder(String text) {
		((Label) this.heatsTbl.getPlaceholder()).setText(text);
	}

}
