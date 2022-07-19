package de.regatta_hd.ui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import de.regatta_hd.commons.core.ConfigService;
import de.regatta_hd.commons.fx.CommonsFXModule;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * The guice module to register UI components.
 */
public class UIModule extends AbstractModule {
	private static final Logger logger = Logger.getLogger(UIModule.class.getName());

	public static final String CONFIG_SHOW_ID_COLUMN = "config.showIdColumn";

	@Override
	protected void configure() {
		install(new CommonsFXModule());
	}

	@Provides
	@Named(CONFIG_SHOW_ID_COLUMN)
	BooleanProperty getShowIdColumn(ConfigService configService) {
		SimpleBooleanProperty property = new SimpleBooleanProperty(
				configService.getBooleanProperty(CONFIG_SHOW_ID_COLUMN));
		property.addListener((obs, oldValue, newValue) -> {
			try {
				configService.setProperty(CONFIG_SHOW_ID_COLUMN, newValue.booleanValue());
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		});
		return property;
	}

}
