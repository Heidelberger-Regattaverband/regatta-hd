package de.regatta_hd.aquarius.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.aquarius.DBConfig;
import de.regatta_hd.aquarius.DBConfigStore;
import de.regatta_hd.common.ConfigService;

@Singleton
public class DBConfigStoreImpl implements DBConfigStore {

	private static final String DB_HOST = "dbHost";
	private static final String DB_NAME = "dbName";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password"; // TODO: don't store password
	private static final String ENCRYPT = "encrypt";
	private static final String TRUST_SERVER_CERTIFICATE = "trustServerCertificate";

	@Inject
	private ConfigService cfgService;

	@Override
	public DBConfig getLastSuccessful() throws IOException {
		return DBConfig.builder() //
				.dbHost(this.cfgService.getProperty(DB_HOST)).dbName(this.cfgService.getProperty(DB_NAME))
				.username(this.cfgService.getProperty(USERNAME)).password(this.cfgService.getProperty(PASSWORD))
				.encrypt(this.cfgService.getBooleanProperty(ENCRYPT))
				.trustServerCertificate(this.cfgService.getBooleanProperty(TRUST_SERVER_CERTIFICATE)).build();
	}

	@Override
	public void setLastSuccessful(DBConfig connectionData) throws IOException {
		requireNonNull(connectionData, "connectionData");

		this.cfgService.setProperty(DB_HOST, connectionData.getDbHost());
		this.cfgService.setProperty(DB_NAME, connectionData.getDbName());
		this.cfgService.setProperty(USERNAME, connectionData.getUsername());
		this.cfgService.setProperty(PASSWORD, connectionData.getPassword());
		this.cfgService.setProperty(ENCRYPT, connectionData.isEncrypt());
		this.cfgService.setProperty(TRUST_SERVER_CERTIFICATE, connectionData.isTrustServerCertificate());
	}
}
