package de.regatta_hd.aquarius.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.DBConfig;
import de.regatta_hd.aquarius.DBConfigStore;
import de.regatta_hd.common.ConfigService;

public class DBConfigStoreImpl implements DBConfigStore {

	private static final String DB_HOST = "dbHost";
	private static final String DB_NAME = "dbName";
	private static final String USERNAME = "userName";
	private static final String PASSWORD = "password"; // TODO: don't store password

	@Inject
	private ConfigService cfgService;

	@Override
	public DBConfig getLastSuccessful() throws IOException {
		return DBConfig.builder() //
				.dbHost(this.cfgService.getProperty(DB_HOST)) //
				.dbName(this.cfgService.getProperty(DB_NAME)) //
				.userName(this.cfgService.getProperty(USERNAME)) //
				.password(this.cfgService.getProperty(PASSWORD)).build();
	}

	@Override
	public void setLastSuccessful(DBConfig connectionData) throws IOException {
		requireNonNull(connectionData, "connectionData");

		this.cfgService.setProperty(DB_HOST, connectionData.getDbHost());
		this.cfgService.setProperty(DB_NAME, connectionData.getDbName());
		this.cfgService.setProperty(USERNAME, connectionData.getUserName());
		this.cfgService.setProperty(PASSWORD, connectionData.getPassword());
	}
}
