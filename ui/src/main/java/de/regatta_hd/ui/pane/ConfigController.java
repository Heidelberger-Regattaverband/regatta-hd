package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import com.fazecast.jSerialComm.SerialPort;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.commons.fx.stage.BaseController;
import de.regatta_hd.ui.UIModule;
import de.regatta_hd.ui.util.SerialPortUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

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
	@Named(UIModule.CONFIG_SERIAL_PORT_TRAFFIC_LIGHT)
	private StringProperty serialPortTrafficLight;

	private final ObservableList<SerialPort> serialPortsStartSignalList = SerialPortUtils.getAllSerialPorts(true);
	private final ObservableList<SerialPort> serialPortsTrafficLightList = SerialPortUtils.getAllSerialPorts(true);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.showIdColumnCbox.selectedProperty().bindBidirectional(this.showIdColumn);

		this.serialPortStartSignalCBox.setItems(this.serialPortsStartSignalList);
		int index = SerialPortUtils.findByPortPath(this.serialPortsStartSignalList, this.serialPortStartSignal.get());
		this.serialPortStartSignalCBox.getSelectionModel().select(index);

		this.serialPortTrafficLightCBox.setItems(this.serialPortsTrafficLightList);
		index = SerialPortUtils.findByPortPath(this.serialPortsTrafficLightList, this.serialPortTrafficLight.get());
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
		this.serialPortTrafficLight.set(serialPort != null ? serialPort.getSystemPortPath() : null);
	}

}
