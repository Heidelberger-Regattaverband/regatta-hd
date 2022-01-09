package de.regatta_hd.common.impl;

import static java.util.Objects.requireNonNull;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import de.regatta_hd.common.ConfigService;

public class ConfigServiceImpl implements ConfigService {

	private static final String KEY_MUST_NOT_BE_NULL = "key must not be null";

	private Properties properties;

	// getter

	@Override
	public synchronized String getProperty(String key) throws IOException {
		return getProperties().getProperty(requireNonNull(key, KEY_MUST_NOT_BE_NULL));
	}

	@Override
	public boolean getBooleanProperty(String key) throws IOException {
		String property = getProperty(key);
		return property != null || Boolean.parseBoolean(property);
	}

	@Override
	public int getIntegerProperty(String key) throws IOException, NumberFormatException {
		String property = getProperty(key);
		return property != null ? Integer.parseInt(property) : 0;
	}

	// setter

	@Override
	public synchronized void setProperty(String key, String value) throws IOException {
		requireNonNull(key, KEY_MUST_NOT_BE_NULL);

		if (value == null) {
			removePropertyImpl(key);
		} else {
			setPropertyImpl(key, value);
		}
	}

	@Override
	public void setProperty(String key, boolean value) throws IOException {
		setPropertyImpl(requireNonNull(key, KEY_MUST_NOT_BE_NULL), Boolean.toString(value));
	}

	@Override
	public void setProperty(String key, int value) throws IOException {
		setPropertyImpl(requireNonNull(key, KEY_MUST_NOT_BE_NULL), Integer.toString(value));
	}

	// cleanup

	@Override
	public synchronized void removeProperty(String key) throws IOException {
		removePropertyImpl(requireNonNull(key, KEY_MUST_NOT_BE_NULL));
	}

	// private

	private void setPropertyImpl(String key, String value) throws IOException {
		getProperties().setProperty(key, value);
		storeProperties();
	}

	private void removePropertyImpl(String key) throws IOException {
		getProperties().remove(key);
		storeProperties();
	}

	private Properties getProperties() throws IOException {
		if (this.properties == null) {
			this.properties = new Properties();
			loadProperties();
		}
		return this.properties;
	}

	private void loadProperties() throws IOException {
		Path settingsPath = getSettingsPath();
		if (Files.exists(settingsPath)) {
			try (InputStream input = Files.newInputStream(settingsPath)) {
				this.properties.load(input);
			}
		}
	}

	private void storeProperties() throws IOException {
		try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(getSettingsPath()))) {
			this.properties.store(output, "Last succesful database connection settings.");
		}
	}

	private static Path getSettingsPath() {
		String userHome = System.getProperty("user.home");
		return Paths.get(userHome, "RegattaHD.properties");
	}
}
