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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The guice module to register UI components.
 */
public class UIModule extends AbstractModule {
	private static final Logger logger = Logger.getLogger(UIModule.class.getName());

	public static final String CONFIG_SHOW_ID_COLUMN = "config.showIdColumn";

	public static final String CONFIG_SERIAL_PORT_NAME = "config.serialPortName";

	private SimpleBooleanProperty showIdColumnProperty;

	private SimpleStringProperty serialPortNameProperty;

	@Override
	protected void configure() {
		install(new CommonsFXModule());
	}

	@Provides
	@Named(CONFIG_SHOW_ID_COLUMN)
	BooleanProperty getShowIdColumn(ConfigService configService) {
		if (this.showIdColumnProperty == null) {
			this.showIdColumnProperty = new SimpleBooleanProperty(
					configService.getBooleanProperty(CONFIG_SHOW_ID_COLUMN));
			this.showIdColumnProperty.addListener((obs, oldValue, newValue) -> {
				try {
					configService.setProperty(CONFIG_SHOW_ID_COLUMN, newValue.booleanValue());
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			});
		}
		return this.showIdColumnProperty;
	}

	@Provides
	@Named(CONFIG_SERIAL_PORT_NAME)
	StringProperty getSerialPortName(ConfigService configService) {
		if (this.serialPortNameProperty == null) {
			this.serialPortNameProperty = new SimpleStringProperty(configService.getProperty(CONFIG_SERIAL_PORT_NAME));
			this.serialPortNameProperty.addListener((obs, oldValue, newValue) -> {
				try {
					configService.setProperty(CONFIG_SERIAL_PORT_NAME, newValue);
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			});
		}
		return this.serialPortNameProperty;
	}

}
