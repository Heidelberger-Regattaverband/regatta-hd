package de.regatta_hd.aquarius.db.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;

import com.google.inject.Singleton;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.DBConfiguration;

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
	public synchronized CriteriaBuilder getCriteriaBuilder() {
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
	public synchronized void open(DBConfiguration connectionData) {
		Objects.requireNonNull(connectionData, "connectionData is null.");

		open(connectionData.getDbHost(), connectionData.getDbName(), connectionData.getUserName(),
				connectionData.getPassword());
	}

	private void checkIsOpen() {
		if (!isOpenImpl()) {
			throw new IllegalStateException("Not connected.");
		}
	}

	private boolean isOpenImpl() {
		return this.entityManager != null && this.entityManager.isOpen();
	}

	private void open(String hostName, String dbName, String userName, String password) {
		close();

		Map<String, String> props = new HashMap<>();
		String url = String.format("jdbc:sqlserver://%s;database=%s",
				Objects.requireNonNull(hostName, "hostName is null."),
				Objects.requireNonNull(dbName, "dbName is null."));
		props.put("javax.persistence.jdbc.url", url);
		props.put("javax.persistence.jdbc.user", Objects.requireNonNull(userName, "userName is null."));
		props.put("javax.persistence.jdbc.password", Objects.requireNonNull(password, "password is null."));

		EntityManagerFactory factory = Persistence.createEntityManagerFactory("aquarius", props);
		this.entityManager = factory.createEntityManager();
	}
}
