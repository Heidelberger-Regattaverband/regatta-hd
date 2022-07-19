package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.ui.UIModule;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class ConfigController extends AbstractBaseController {

	@FXML
	private CheckBox showIdColumnCbox;

	@Inject
	@Named(UIModule.CONFIG_SHOW_ID_COLUMN)
	private BooleanProperty showIdColumn;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.showIdColumnCbox.selectedProperty().bindBidirectional(this.showIdColumn);
	}

	@Override
	public void shutdown() {
		// nothing to clean-up yet
	}

}
