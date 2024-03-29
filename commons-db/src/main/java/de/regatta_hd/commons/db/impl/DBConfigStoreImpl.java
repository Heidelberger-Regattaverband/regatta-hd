package de.regatta_hd.commons.db.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.commons.core.ConfigService;
import de.regatta_hd.commons.db.DBConfig;
import de.regatta_hd.commons.db.DBConfigStore;

@Singleton
public class DBConfigStoreImpl implements DBConfigStore {

	private static final String DB_HOST = "dbHost";
	private static final String DB_NAME = "dbName";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String ENCRYPT = "encrypt";
	private static final String TRUST_SERVER_CERTIFICATE = "trustServerCertificate";
	private static final String UPDATE_SCHEMA = "updateSchema";

	@Inject
	private ConfigService cfgService;

	@Override
	public DBConfig getLastSuccessful() throws IOException {
		return DBConfig.builder() //
				.host(this.cfgService.getProperty(DB_HOST)) //
				.name(this.cfgService.getProperty(DB_NAME)) //
				.user(this.cfgService.getProperty(USERNAME)) //
				.password(this.cfgService.getProperty(PASSWORD)) //
				.encrypt(this.cfgService.getBooleanProperty(ENCRYPT)) //
				.trustServerCertificate(this.cfgService.getBooleanProperty(TRUST_SERVER_CERTIFICATE))
				.updateSchema(this.cfgService.getBooleanProperty(UPDATE_SCHEMA)).build();
	}

	@Override
	public void setLastSuccessful(DBConfig dbConfig) throws IOException {
		requireNonNull(dbConfig, "dbConfig");

		this.cfgService.setProperty(DB_HOST, dbConfig.getHost());
		this.cfgService.setProperty(DB_NAME, dbConfig.getName());
		this.cfgService.setProperty(USERNAME, dbConfig.getUser());
		this.cfgService.setProperty(ENCRYPT, dbConfig.isEncrypt());
		this.cfgService.setProperty(TRUST_SERVER_CERTIFICATE, dbConfig.isTrustServerCertificate());
		this.cfgService.setProperty(UPDATE_SCHEMA, dbConfig.isUpdateSchema());

		this.cfgService.removeProperty(PASSWORD);
	}
}
