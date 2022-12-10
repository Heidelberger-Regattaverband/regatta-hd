package de.regatta_hd.ui;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.regatta_hd.commons.core.ConfigService;
import de.regatta_hd.commons.fx.CommonsFXModule;
import de.regatta_hd.ui.util.TrafficLightsStartList;

/**
 * The guice module to register UI components.
 */
public class UIModule extends AbstractModule {
	private static final Logger logger = Logger.getLogger(UIModule.class.getName());

	public static final String CONFIG_SHOW_ID_COLUMN = "config.showIdColumn";

	public static final String CONFIG_SERIAL_PORT_START_SIGNAL = "config.serialPort.startSignal";

	public static final String CONFIG_SERIAL_PORT_TRAFFIC_LIGHTS = "config.serialPort.trafficLights";

	private static final ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.GERMANY);

	@Override
	protected void configure() {
		bind(TrafficLightsStartList.class);
		install(new CommonsFXModule());
	}

	@Provides
	@Named(CONFIG_SHOW_ID_COLUMN)
	@Singleton
	BooleanProperty getShowIdColumn(ConfigService configService) {
		SimpleBooleanProperty showIdColumn = new SimpleBooleanProperty(
				configService.getBooleanProperty(CONFIG_SHOW_ID_COLUMN));
		showIdColumn.addListener((obs, oldValue, newValue) -> {
			try {
				configService.setProperty(CONFIG_SHOW_ID_COLUMN, newValue.booleanValue());
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		});
		return showIdColumn;
	}

	@Provides
	@Named(CONFIG_SERIAL_PORT_START_SIGNAL)
	@Singleton
	StringProperty getSerialPortStartSignal(ConfigService configService) {
		SimpleStringProperty serialPortStartSignal = new SimpleStringProperty(
				configService.getProperty(CONFIG_SERIAL_PORT_START_SIGNAL));
		serialPortStartSignal.addListener((obs, oldValue, newValue) -> {
			try {
				configService.setProperty(CONFIG_SERIAL_PORT_START_SIGNAL, newValue);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		});
		return serialPortStartSignal;
	}

	@Provides
	@Named(CONFIG_SERIAL_PORT_TRAFFIC_LIGHTS)
	@Singleton
	StringProperty getSerialPortTrafficLight(ConfigService configService) {
		SimpleStringProperty serialPortTrafficLight = new SimpleStringProperty(
				configService.getProperty(CONFIG_SERIAL_PORT_TRAFFIC_LIGHTS));
		serialPortTrafficLight.addListener((obs, oldValue, newValue) -> {
			try {
				configService.setProperty(CONFIG_SERIAL_PORT_TRAFFIC_LIGHTS, newValue);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		});
		return serialPortTrafficLight;
	}

	@Provides
	@Singleton
	ResourceBundle getResourceBundle() {
		return bundle;
	}
}
