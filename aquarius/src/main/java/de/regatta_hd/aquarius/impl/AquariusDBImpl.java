package de.regatta_hd.aquarius.impl;

import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import de.regatta_hd.aquarius.model.MetaData;
import de.regatta_hd.commons.core.ListenerManager;
import de.regatta_hd.commons.db.DBConfig;
import de.regatta_hd.commons.db.DBConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

@Singleton
public class AquariusDBImpl implements DBConnection {

	// executes database operations concurrent to JavaFX operations.
	private static ExecutorService databaseExecutor = Executors.newFixedThreadPool(1, new DatabaseThreadFactory());

	@Inject
	private ListenerManager listenerManager;

	private String version;

	private EntityManagerFactory factory;

	@Override
	public synchronized void close() {
		if (isOpenImpl()) {
			if (this.factory != null) {
				this.factory.close();
				this.factory = null;
			}
			this.version = null;

			// notify listeners about changed AquariusDB state
			notifyListeners(new AquariusDBStateChangedEventImpl(this));
		}
	}

	String getVersion() {
		return this.version;
	}

	@Override
	public synchronized EntityManager getEntityManager() {
		checkIsOpen();
		return this.factory.createEntityManager();
	}

	@Override
	public synchronized boolean isOpen() {
		return isOpenImpl();
	}

	@Override
	public synchronized void open(DBConfig dbCfg) throws SQLServerException {
		Map<String, String> props = getProperties(requireNonNull(dbCfg, "dbCfg must not be null"));

		close();

		try {
			this.factory = Persistence.createEntityManagerFactory("aquarius", props);

			this.version = readVersion();

			// notify listeners about changed AquariusDB state
			notifyListeners(new AquariusDBStateChangedEventImpl(this));
		} catch (PersistenceException e) {
			if (this.factory != null) {
				this.factory.close();
				this.factory = null;
			}
			Throwable rootCause = ExceptionUtils.getRootCause(e);
			if (rootCause instanceof SQLServerException) {
				throw (SQLServerException) rootCause;
			}
			throw e;
		}
	}

	@SuppressWarnings("resource")
	@Override
	public void updateSchema() {
		checkIsOpen();

		Session session = getEntityManager().unwrap(Session.class);
		session.doWork(connection -> {
			try {
				Database database = DatabaseFactory.getInstance()
						.findCorrectDatabaseImplementation(new JdbcConnection(connection));
				database.setDatabaseChangeLogLockTableName("HRV_ChangeLogLock");
				database.setDatabaseChangeLogTableName("HRV_ChangeLog");

				Liquibase liquibase = new Liquibase("/db/liquibase-changeLog.xml", new ClassLoaderResourceAccessor(),
						database);
				liquibase.update(new Contexts(), new LabelExpression());
			} catch (LiquibaseException e) {
				if (this.factory != null) {
					this.factory.close();
					this.factory = null;
				}
				throw new SQLException(e);
			}
		});
	}

	@Override
	public ExecutorService getExecutor() {
		return databaseExecutor;
	}

	private void checkIsOpen() {
		if (!isOpenImpl()) {
			throw new IllegalStateException("Not connected.");
		}
		if (!Thread.currentThread().getName().startsWith("Database-Connection")) {
			throw new IllegalThreadStateException("Not DB session thread.");
		}
	}

	private boolean isOpenImpl() {
		return this.factory != null && this.factory.isOpen();
	}

	private String readVersion() {
		TypedQuery<MetaData> query = this.getEntityManager()
				.createQuery("SELECT m FROM MetaData m WHERE m.key = 'PatchLevel'", MetaData.class);
		MetaData metaData = query.getSingleResult();
		return metaData.getValue();
	}

	private void notifyListeners(AquariusDBStateChangedEventImpl event) {
		List<StateChangedEventListener> listeners = this.listenerManager.getListeners(StateChangedEventListener.class);
		for (StateChangedEventListener listener : listeners) {
			listener.stateChanged(event);
		}
	}

	private static Map<String, String> getProperties(DBConfig dbCfg) {
		Map<String, String> props = new HashMap<>();
		String url = String.format("jdbc:sqlserver://%s;databaseName=%s;encrypt=%s", dbCfg.getDbHost(),
				dbCfg.getDbName(), Boolean.toString(dbCfg.isEncrypt()));

		if (dbCfg.isEncrypt() && dbCfg.isTrustServerCertificate()) {
			url += ";trustServerCertificate=true";
		}

		props.put("javax.persistence.jdbc.url", url);
		props.put("javax.persistence.jdbc.user", dbCfg.getUsername());
		props.put("javax.persistence.jdbc.password", dbCfg.getPassword());
		return props;
	}

	private static class DatabaseThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable, "Database-Connection-" + poolNumber.getAndIncrement() + "-thread");
			thread.setDaemon(true);
			return thread;
		}
	}
}
