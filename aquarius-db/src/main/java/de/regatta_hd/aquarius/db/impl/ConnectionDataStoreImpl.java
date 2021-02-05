package de.regatta_hd.aquarius.db.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import de.regatta_hd.aquarius.db.ConnectionData;
import de.regatta_hd.aquarius.db.ConnectionDataStore;

public class ConnectionDataStoreImpl implements ConnectionDataStore {

	private static final String DB_HOST = "dbHost";
	private static final String DB_NAME = "dbName";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password"; // TODO: don't store password

	@Override
	public ConnectionData getLastSuccessful() throws IOException {
		Path settingsPath = getSettingsPath();

		if (Files.exists(settingsPath)) {
			Properties props = new Properties();
			try (InputStream input = Files.newInputStream(settingsPath)) {
				props.load(input);
			}
			return ConnectionData.builder() //
					.dbHost(props.getProperty(DB_HOST)) //
					.dbName(props.getProperty(DB_NAME)) //
					.userName(props.getProperty(USERNAME)) //
					.password(props.getProperty(PASSWORD)).build();
		}
		return null;
	}

	@Override
	public void setLastSuccessful(ConnectionData connectionData) throws IOException {
		Objects.requireNonNull(connectionData, "connectionData");

		Properties props = new Properties();
		props.setProperty(DB_HOST, connectionData.getDbHost());
		props.setProperty(DB_NAME, connectionData.getDbName());
		props.setProperty(USERNAME, connectionData.getUserName());
		props.setProperty(PASSWORD, connectionData.getPassword());

		try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(getSettingsPath()))) {
			props.store(output, "Last succesful database connection settings.");
		}
	}

	private Path getSettingsPath() {
		String userHome = System.getProperty("user.home");
		return Paths.get(userHome, "RegattaHD.properties");
	}
}
