package de.regatta_hd.ui.util;

import com.fazecast.jSerialComm.SerialPort;

import javafx.util.StringConverter;

public class SerialPortStringConverter extends StringConverter<SerialPort> {

	@Override
	public String toString(SerialPort port) {
		if (port == null) {
			return null;
		}
		return port.getDescriptivePortName() + ", " + port.getSystemPortName() + ", " + port.getSystemPortPath();
	}

	@Override
	public SerialPort fromString(String string) {
		return null;
	}
}