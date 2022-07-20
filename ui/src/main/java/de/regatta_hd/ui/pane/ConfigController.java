package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import com.fazecast.jSerialComm.SerialPort;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.ui.UIModule;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

public class ConfigController extends AbstractBaseController {

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

	private final ObservableList<SerialPort> serialPortsStartSignalList = FXCollections.observableArrayList();
	private final ObservableList<SerialPort> serialPortsTrafficLightList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.showIdColumnCbox.selectedProperty().bindBidirectional(this.showIdColumn);

		this.serialPortsStartSignalList.addAll(SerialPort.getCommPorts());
		this.serialPortStartSignalCBox.setItems(this.serialPortsStartSignalList);
		int index = -1;
		for (int i = 0; i < this.serialPortsStartSignalList.size(); i++) {
			if (this.serialPortsStartSignalList.get(i).getSystemPortPath().equals(this.serialPortStartSignal.get())) {
				index = i;
				break;
			}
		}
		this.serialPortStartSignalCBox.getSelectionModel().select(index);

		this.serialPortsTrafficLightList.addAll(SerialPort.getCommPorts());
		this.serialPortTrafficLightCBox.setItems(this.serialPortsStartSignalList);
		index = -1;
		for (int i = 0; i < this.serialPortsTrafficLightList.size(); i++) {
			if (this.serialPortsTrafficLightList.get(i).getSystemPortPath().equals(this.serialPortTrafficLight.get())) {
				index = i;
				break;
			}
		}
		this.serialPortTrafficLightCBox.getSelectionModel().select(index);
	}

	@Override
	public void shutdown() {
		// nothing to clean-up yet
	}

	@FXML
	void handleSerialPortStartSignalOnAction() {
		SerialPort serialPort = this.serialPortStartSignalCBox.getSelectionModel().getSelectedItem();
		if (serialPort != null) {
			this.serialPortStartSignal.set(serialPort.getSystemPortPath());
		}
	}

	@FXML
	void handleSerialPortTrafficLightOnAction() {
		SerialPort serialPort = this.serialPortTrafficLightCBox.getSelectionModel().getSelectedItem();
		if (serialPort != null) {
			this.serialPortTrafficLight.set(serialPort.getSystemPortPath());
		}
	}

}
