package de.regatta_hd.aquarius.impl;

import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;

import com.google.inject.Singleton;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.aquarius.DBConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
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

	private Thread sessionThread;

	@Override
	public synchronized void close() {
		if (isOpenImpl()) {
			this.entityManager.close();
			this.entityManager = null;
			this.sessionThread = null;
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

	@SuppressWarnings("resource")
	@Override
	public synchronized void open(DBConfig dbCfg) {
		requireNonNull(dbCfg, "dbCfg must not be null");

		close();

		Map<String, String> props = getProperties(dbCfg);
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("aquarius", props);
		this.entityManager = factory.createEntityManager();

		if (this.entityManager.isOpen()) {
			Session session = this.entityManager.unwrap(Session.class);
			session.doWork(connection -> {
				try {
					Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
					Liquibase liquibase = new Liquibase("/db/liquibase-changeLog.xml", new ClassLoaderResourceAccessor(), database);
					liquibase.update(new Contexts(), new LabelExpression());

					// store current thread to ensure further DB access is done in same thread
					this.sessionThread = Thread.currentThread();
				} catch (LiquibaseException e) {
					this.entityManager = null;
					throw new SQLException(e);
				}
			});
		}
	}

	@Override
	public synchronized EntityTransaction beginTransaction() {
		EntityTransaction entityTransaction = getEntityManager().getTransaction();
		if (!entityTransaction.isActive()) {
			entityTransaction.begin();
		}
		return entityTransaction;
	}

	private void checkIsOpen() {
		if (!isOpenImpl()) {
			throw new IllegalStateException("Not connected.");
		}
		if (Thread.currentThread() != this.sessionThread) {
			throw new IllegalThreadStateException("Not DB session thread.");
		}
	}

	private boolean isOpenImpl() {
		return this.entityManager != null && this.entityManager.isOpen();
	}

	private static Map<String, String> getProperties(DBConfig dbCfg) {
		Map<String, String> props = new HashMap<>();
		String url = String.format("jdbc:sqlserver://%s;databaseName=%s", dbCfg.getDbHost(), dbCfg.getDbName());

		if (dbCfg.isEncrypt()) {
			url += ";encrypt=true";
			if (dbCfg.isTrustServerCertificate()) {
				url += ";trustServerCertificate=true";
			}
		}

		props.put("javax.persistence.jdbc.url", url);
		props.put("javax.persistence.jdbc.user", dbCfg.getUsername());
		props.put("javax.persistence.jdbc.password", dbCfg.getPassword());
		return props;
	}
}
