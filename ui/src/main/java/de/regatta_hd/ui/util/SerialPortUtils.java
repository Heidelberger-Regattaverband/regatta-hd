package de.regatta_hd.ui.util;

import java.util.Arrays;
import java.util.Optional;

import com.fazecast.jSerialComm.SerialPort;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SerialPortUtils {

	private SerialPortUtils() {
		// avoid instances
	}

	public static ObservableList<SerialPort> getAllSerialPorts(boolean withNullEntry) {
		ObservableList<SerialPort> serialPorts = FXCollections.observableArrayList();
		if (withNullEntry) {
			serialPorts.add(null);
		}
		serialPorts.addAll(SerialPort.getCommPorts());
		return serialPorts;
	}

	public static int findByPortPath(ObservableList<SerialPort> serialPorts, String portPath) {
		int index = -1;
		for (int i = 0; i < serialPorts.size(); i++) {
			SerialPort serialPort = serialPorts.get(i);
			if (serialPort != null && serialPort.getSystemPortPath().equals(portPath)) {
				index = i;
				break;
			}
		}
		return index;
	}

	public static Optional<SerialPort> getSerialPortByPath(String portPath) {
		return Arrays.asList(SerialPort.getCommPorts()).stream()
				.filter(serialPort -> serialPort.getSystemPortPath().equals(portPath)).findFirst();
	}

}
