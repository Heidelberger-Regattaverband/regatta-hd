package de.regatta_hd.aquarius.db.impl;

import java.io.IOException;
import java.util.Objects;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.db.DBConfiguration;
import de.regatta_hd.aquarius.db.DBConfigurationStore;
import de.regatta_hd.common.ConfigService;

public class DBConfigurationStoreImpl implements DBConfigurationStore {

	private static final String DB_HOST = "dbHost";
	private static final String DB_NAME = "dbName";
	private static final String USERNAME = "userName";
	private static final String PASSWORD = "password"; // TODO: don't store password

	@Inject
	private ConfigService cfgService;

	@Override
	public DBConfiguration getLastSuccessful() throws IOException {
		return DBConfiguration.builder() //
				.dbHost(this.cfgService.getProperty(DB_HOST)) //
				.dbName(this.cfgService.getProperty(DB_NAME)) //
				.userName(this.cfgService.getProperty(USERNAME)) //
				.password(this.cfgService.getProperty(PASSWORD)).build();
	}

	@Override
	public void setLastSuccessful(DBConfiguration connectionData) throws IOException {
		Objects.requireNonNull(connectionData, "connectionData");

		this.cfgService.setProperty(DB_HOST, connectionData.getDbHost());
		this.cfgService.setProperty(DB_NAME, connectionData.getDbName());
		this.cfgService.setProperty(USERNAME, connectionData.getUserName());
		this.cfgService.setProperty(PASSWORD, connectionData.getPassword());
	}
}
