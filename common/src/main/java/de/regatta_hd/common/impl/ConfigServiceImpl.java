package de.regatta_hd.common.impl;

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

	private Properties properties;

	@Override
	public String getProperty(String name) throws IOException {
		return getProperties().getProperty(name);
	}

	@Override
	public void setProperty(String key, String value) throws IOException {
		getProperties().setProperty(key, value);
		try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(getSettingsPath()))) {
			this.properties.store(output, "Last succesful database connection settings.");
		}
	}

	private Properties getProperties() throws IOException {
		if (this.properties == null) {
			this.properties = new Properties();

			Path settingsPath = getSettingsPath();
			if (Files.exists(settingsPath)) {
				try (InputStream input = Files.newInputStream(settingsPath)) {
					this.properties.load(input);
				}
			}
		}
		return this.properties;
	}

	private static Path getSettingsPath() {
		String userHome = System.getProperty("user.home");
		return Paths.get(userHome, "RegattaHD.properties");
	}
}
