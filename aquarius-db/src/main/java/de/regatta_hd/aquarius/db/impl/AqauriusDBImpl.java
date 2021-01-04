package de.regatta_hd.aquarius.db.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.ConnectionData;

@Singleton
public class AqauriusDBImpl implements AquariusDB {

	private EntityManager entityManager;

	@Override
	public synchronized void close() {
		if (isOpenImpl()) {
			this.entityManager.close();
			this.entityManager = null;
		}
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return getEntityManager().getCriteriaBuilder();
	}

	@Override
	public synchronized EntityManager getEntityManager() {
		checkIsOpen();
		return this.entityManager;
	}

	@Override
	public synchronized boolean isOpen() {
		return isOpenImpl();
	}

	@Override
	public void open(ConnectionData connectionData) {
		open(connectionData.getHostName(), connectionData.getDbName(), connectionData.getUserName(),
				connectionData.getPassword());
	}

	@Override
	public synchronized void open(String hostName, String dbName, String userName, String password) {
		Objects.requireNonNull(hostName, "hostName is null.");
		Objects.requireNonNull(dbName, "dbName is null.");
		Objects.requireNonNull(userName, "userName is null.");
		Objects.requireNonNull(password, "password is null.");

		close();

		Map<String, String> props = new HashMap<>();
		String url = String.format("jdbc:sqlserver://%s;database=%s", hostName, dbName);
		props.put("javax.persistence.jdbc.url", url);
		props.put("javax.persistence.jdbc.user", userName);
		props.put("javax.persistence.jdbc.password", password);

		EntityManagerFactory factory = Persistence.createEntityManagerFactory("aquarius", props);
		this.entityManager = factory.createEntityManager();
	}

	private void checkIsOpen() {
		if (!isOpenImpl()) {
			throw new IllegalStateException("Not connected.");
		}
	}

	private boolean isOpenImpl() {
		return this.entityManager != null && this.entityManager.isOpen();
	}
}
