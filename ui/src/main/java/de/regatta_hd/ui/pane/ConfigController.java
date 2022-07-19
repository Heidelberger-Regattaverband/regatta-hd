package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.regatta_hd.commons.fx.stage.Controller;
import de.regatta_hd.ui.UIModule;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;

public class ConfigController implements Initializable, Controller {

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
		// TODO Auto-generated method stub
	}

}
