package de.regatta_hd.aquarius.db.impl;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;

import com.google.inject.Singleton;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.DBConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaBuilder;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

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
		requireNonNull(connectionData, "connectionData is null.");

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
		String url = String.format("jdbc:sqlserver://%s;database=%s", requireNonNull(hostName, "hostName is null."),
				requireNonNull(dbName, "dbName is null."));
		props.put("javax.persistence.jdbc.url", url);
		props.put("javax.persistence.jdbc.user", requireNonNull(userName, "userName is null."));
		props.put("javax.persistence.jdbc.password", requireNonNull(password, "password is null."));

		EntityManagerFactory factory = Persistence.createEntityManagerFactory("aquarius", props);
		this.entityManager = factory.createEntityManager();

		if (this.entityManager.isOpen()) {
			Session session = this.entityManager.unwrap(Session.class);
			session.doWork(connection -> {
				try {
					Database database = DatabaseFactory.getInstance()
							.findCorrectDatabaseImplementation(new JdbcConnection(connection));
					Liquibase liquibase = new Liquibase("/db/liquibase-changeLog.xml",
							new ClassLoaderResourceAccessor(), database);
					liquibase.update(new Contexts(), new LabelExpression());
				} catch (LiquibaseException e) {
					e.printStackTrace();
				}
			});
		}
	}
}
