package de.regatta_hd.commons.core.impl;

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import com.google.inject.Singleton;

import de.regatta_hd.commons.core.ConfigService;

@Singleton
public class ConfigServiceImpl implements ConfigService {
	private static final String KEY_MUST_NOT_BE_NULL = "key must not be null";

	private final Properties properties = new Properties();

	private final Path propertiesPath;

	public ConfigServiceImpl() throws IOException {
		String userHome = System.getProperty("user.home");
		this.propertiesPath = Paths.get(userHome, "RegattaHD.properties");
		loadProperties();
	}

	ConfigServiceImpl(Path path) throws IOException {
		this.propertiesPath = path;
		loadProperties();
	}

	// getter

	@Override
	public synchronized String getProperty(String key) {
		return this.properties.getProperty(requireNonNull(key, KEY_MUST_NOT_BE_NULL));
	}

	@Override
	public boolean getBooleanProperty(String key) {
		String property = getProperty(key);
		return property != null && Boolean.parseBoolean(property);
	}

	@Override
	public Integer getIntegerProperty(String key) throws NumberFormatException {
		String property = getProperty(key);
		return property != null ? Integer.valueOf(property) : null;
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
		this.properties.setProperty(key, value);
		storeProperties();
	}

	private void removePropertyImpl(String key) throws IOException {
		this.properties.remove(key);
		storeProperties();
	}

	private void loadProperties() throws IOException {
		if (Files.exists(this.propertiesPath)) {
			try (BufferedReader reader = Files.newBufferedReader(this.propertiesPath, StandardCharsets.UTF_8)) {
				this.properties.load(reader);
			}
		}
	}

	private void storeProperties() throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(this.propertiesPath, StandardCharsets.UTF_8)) {
			this.properties.store(writer, "Last successful database connection settings.");
		}
	}
}
