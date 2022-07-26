package de.regatta_hd.ui;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import de.regatta_hd.commons.core.ConfigService;
import de.regatta_hd.commons.fx.CommonsFXModule;
import de.regatta_hd.ui.util.TrafficLightsStartList;
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

	public static final String CONFIG_SERIAL_PORT_START_SIGNAL = "config.serialPort.startSignal";

	public static final String CONFIG_SERIAL_PORT_TRAFFIC_LIGHT = "config.serialPort.trafficLight";

	private static final ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.GERMANY);

	private SimpleBooleanProperty showIdColumn;

	private SimpleStringProperty serialPortStartSignal;

	private SimpleStringProperty serialPortTrafficLight;

	@Override
	protected void configure() {
		bind(TrafficLightsStartList.class);
		install(new CommonsFXModule());
	}

	@Provides
	@Named(CONFIG_SHOW_ID_COLUMN)
	BooleanProperty getShowIdColumn(ConfigService configService) {
		if (this.showIdColumn == null) {
			this.showIdColumn = new SimpleBooleanProperty(configService.getBooleanProperty(CONFIG_SHOW_ID_COLUMN));
			this.showIdColumn.addListener((obs, oldValue, newValue) -> {
				try {
					configService.setProperty(CONFIG_SHOW_ID_COLUMN, newValue.booleanValue());
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			});
		}
		return this.showIdColumn;
	}

	@Provides
	@Named(CONFIG_SERIAL_PORT_START_SIGNAL)
	StringProperty getSerialPortStartSignal(ConfigService configService) {
		if (this.serialPortStartSignal == null) {
			this.serialPortStartSignal = new SimpleStringProperty(
					configService.getProperty(CONFIG_SERIAL_PORT_START_SIGNAL));
			this.serialPortStartSignal.addListener((obs, oldValue, newValue) -> {
				try {
					configService.setProperty(CONFIG_SERIAL_PORT_START_SIGNAL, newValue);
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			});
		}
		return this.serialPortStartSignal;
	}

	@Provides
	@Named(CONFIG_SERIAL_PORT_TRAFFIC_LIGHT)
	StringProperty getSerialPortTrafficLight(ConfigService configService) {
		if (this.serialPortTrafficLight == null) {
			this.serialPortTrafficLight = new SimpleStringProperty(
					configService.getProperty(CONFIG_SERIAL_PORT_TRAFFIC_LIGHT));
			this.serialPortTrafficLight.addListener((obs, oldValue, newValue) -> {
				try {
					configService.setProperty(CONFIG_SERIAL_PORT_TRAFFIC_LIGHT, newValue);
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			});
		}
		return this.serialPortTrafficLight;
	}

	@Provides
	ResourceBundle getResourceBundle() {
		return bundle;
	}
}
