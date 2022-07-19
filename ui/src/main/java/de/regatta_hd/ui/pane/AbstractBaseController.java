package de.regatta_hd.ui.pane;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.fx.db.DBTaskRunner;
import de.regatta_hd.commons.fx.stage.Controller;
import javafx.fxml.Initializable;

abstract class AbstractBaseController implements Initializable, Controller {

	protected URL location;
	protected ResourceBundle resources;

	@Inject
	protected DBTaskRunner dbTaskRunner;
	@Inject
	protected DBConnection db;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.location = requireNonNull(location, "location must not be null");
		this.resources = requireNonNull(resources, "resources must not be null");
	}

	protected String getText(String key, Object... args) {
		String text = this.resources.getString(key);
		if (args.length > 0) {
			text = MessageFormat.format(text, args);
		}
		return text;
	}

}
