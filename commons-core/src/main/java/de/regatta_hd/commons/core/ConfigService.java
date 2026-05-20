package de.regatta_hd.commons.core;

import java.io.IOException;

import de.regatta_hd.commons.core.impl.ConfigServiceImpl;

public interface ConfigService {

	/**
	 * Creates a new {@link ConfigService} instance backed by the user's default
	 * configuration file ({@code ~/RegattaHD.properties}). This factory exists so
	 * that callers outside of {@code de.regatta_hd.commons.core} do not need to
	 * depend on the internal {@code impl} package.
	 *
	 * @return a fresh, file-backed {@link ConfigService} instance
	 * @throws IOException if the configuration file cannot be read
	 */
	static ConfigService createDefault() throws IOException {
		return new ConfigServiceImpl();
	}

	// getter

	String getProperty(String key);

	boolean getBooleanProperty(String key);

	Integer getIntegerProperty(String key) throws NumberFormatException;

	// setter

	void setProperty(String key, String value) throws IOException;

	void setProperty(String key, int value) throws IOException;

	void setProperty(String key, boolean value) throws IOException;

	// cleanup

	void removeProperty(String key) throws IOException;
}
