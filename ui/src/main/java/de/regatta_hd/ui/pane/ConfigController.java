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
	private ComboBox<SerialPort> serialPortsCBox;

	@Inject
	@Named(UIModule.CONFIG_SHOW_ID_COLUMN)
	private BooleanProperty showIdColumn;
	@Inject
	@Named(UIModule.CONFIG_SERIAL_PORT_NAME)
	private StringProperty serialPortName;

	private final ObservableList<SerialPort> serialPortsList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.showIdColumnCbox.selectedProperty().bindBidirectional(this.showIdColumn);

		this.serialPortsList.addAll(SerialPort.getCommPorts());
		this.serialPortsCBox.setItems(this.serialPortsList);
	}

	@Override
	public void shutdown() {
		// nothing to clean-up yet
	}

}
