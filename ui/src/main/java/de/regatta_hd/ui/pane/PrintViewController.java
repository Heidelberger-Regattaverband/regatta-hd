package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.print.Printer;
import javafx.scene.control.ComboBox;

public class PrintViewController extends AbstractBaseController {

	@FXML
	private ComboBox<Printer> printersCbo;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		loadPrinters();
	}

	@Override
	protected void shutdown() {
		// nothing to shutdown yet
	}

	private void loadPrinters() {
		ObservableSet<Printer> allPrinters = Printer.getAllPrinters();
		this.printersCbo.getItems().setAll(allPrinters);
	}
}
