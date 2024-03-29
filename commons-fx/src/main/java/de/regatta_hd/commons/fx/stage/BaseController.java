package de.regatta_hd.commons.fx.stage;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;

public abstract class BaseController implements Initializable, Controller {

	protected URL rootLocation;
	protected ResourceBundle bundle;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.rootLocation = requireNonNull(location, "location must not be null");
		this.bundle = requireNonNull(resources, "resources must not be null");
	}

	protected String getText(String key, Object... args) {
		String text = this.bundle.getString(key);
		if (args.length > 0) {
			text = MessageFormat.format(text, args);
		}
		return text;
	}

}
