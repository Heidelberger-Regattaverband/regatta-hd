package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

import com.fazecast.jSerialComm.SerialPort;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.commons.fx.stage.BaseController;
import de.regatta_hd.ui.UIModule;
import de.regatta_hd.ui.util.SerialPortUtils;

public class ConfigController extends BaseController {

	@FXML
	private CheckBox showIdColumnCbox;
	@FXML
	private ComboBox<SerialPort> serialPortStartSignalCBox;
	@FXML
	private ComboBox<SerialPort> serialPortTrafficLightCBox;

	@Inject
	@Named(UIModule.CONFIG_SHOW_ID_COLUMN)
	private BooleanProperty showIdColumn;
	@Inject
	@Named(UIModule.CONFIG_SERIAL_PORT_START_SIGNAL)
	private StringProperty serialPortStartSignal;
	@Inject
	@Named(UIModule.CONFIG_SERIAL_PORT_TRAFFIC_LIGHTS)
	private StringProperty serialPortTrafficLights;

	private final ObservableList<SerialPort> serialPortsStartSignalList = SerialPortUtils.getAllSerialPorts(true);
	private final ObservableList<SerialPort> serialPortsTrafficLightsList = SerialPortUtils.getAllSerialPorts(true);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.showIdColumnCbox.selectedProperty().bindBidirectional(this.showIdColumn);

		this.serialPortStartSignalCBox.setItems(this.serialPortsStartSignalList);
		int index = SerialPortUtils.findByPortPath(this.serialPortsStartSignalList, this.serialPortStartSignal.get());
		this.serialPortStartSignalCBox.getSelectionModel().select(index);

		this.serialPortTrafficLightCBox.setItems(this.serialPortsTrafficLightsList);
		index = SerialPortUtils.findByPortPath(this.serialPortsTrafficLightsList, this.serialPortTrafficLights.get());
		this.serialPortTrafficLightCBox.getSelectionModel().select(index);
	}

	@Override
	public void shutdown() {
		// nothing to clean-up yet
	}

	@FXML
	void handleSerialPortStartSignalOnAction() {
		SerialPort serialPort = this.serialPortStartSignalCBox.getSelectionModel().getSelectedItem();
		this.serialPortStartSignal.set(serialPort != null ? serialPort.getSystemPortPath() : null);
	}

	@FXML
	void handleSerialPortTrafficLightOnAction() {
		SerialPort serialPort = this.serialPortTrafficLightCBox.getSelectionModel().getSelectedItem();
		this.serialPortTrafficLights.set(serialPort != null ? serialPort.getSystemPortPath() : null);
	}

}
