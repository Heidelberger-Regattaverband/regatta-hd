package de.regatta_hd.aquarius.impl;

import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import de.regatta_hd.aquarius.model.MetaData;
import de.regatta_hd.commons.core.ListenerManager;
import de.regatta_hd.commons.db.DBConfig;
import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.db.DBThreadPoolExecutor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
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

	private final ThreadLocal<EntityManager> entityManager = ThreadLocal
			.withInitial(() -> this.emFactory.createEntityManager());
	private final ListenerManager listenerManager;

	private ExecutorService dbExecutor;
	private String version;
	private EntityManagerFactory emFactory;

	@Inject
	public AquariusDBImpl(ListenerManager listenerManager) {
		this.listenerManager = requireNonNull(listenerManager, "listenerManager must not be null");
	}

	@Override
	public synchronized ExecutorService getExecutor() {
		if (this.dbExecutor == null) {
			this.dbExecutor = createExecutor();
		}
		return this.dbExecutor;
	}

	@Override
	public synchronized void close() {
		if (isOpenImpl()) {
			this.entityManager.remove();
			if (this.emFactory != null) {
				this.emFactory.close();
				this.emFactory = null;
			}
			if (this.dbExecutor != null) {
				this.dbExecutor.shutdownNow();
				this.dbExecutor = null;
			}
			this.version = null;

			// notify listeners about changed AquariusDB state
			notifyListeners(new AquariusDBStateChangedEventImpl(this));
		}
	}

	@Override
	public synchronized EntityManager getEntityManager() {
		ensureOpen();
		return this.entityManager.get();
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
			this.emFactory = Persistence.createEntityManagerFactory("aquarius", props);

			this.version = readVersion();

			// notify listeners about changed AquariusDB state
			notifyListeners(new AquariusDBStateChangedEventImpl(this));
		} catch (PersistenceException e) {
			if (this.emFactory != null) {
				this.emFactory.close();
				this.emFactory = null;
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
		ensureOpen();

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
				if (this.emFactory != null) {
					this.emFactory.close();
					this.emFactory = null;
				}
				throw new SQLException(e);
			}
		});
	}

	String getVersion() {
		return this.version;
	}

	private void ensureOpen() {
		if (!isOpenImpl()) {
			throw new IllegalStateException("Not connected.");
		}
		if (!Thread.currentThread().getName().startsWith(DBThreadPoolExecutor.DB_THREAD_PREFIX)) {
			throw new IllegalStateException("Not a Database connection thread.");
		}
	}

	private boolean isOpenImpl() {
		return this.emFactory != null && this.emFactory.isOpen();
	}

	private String readVersion() {
		MetaData metaData = this.getEntityManager()
				.createQuery("SELECT m FROM MetaData m WHERE m.key = 'PatchLevel'", MetaData.class).getSingleResult();
		return metaData.getValue();
	}

	private void notifyListeners(AquariusDBStateChangedEventImpl event) {
		List<StateChangedEventListener> listeners = this.listenerManager.getListeners(StateChangedEventListener.class);
		for (StateChangedEventListener listener : listeners) {
			listener.stateChanged(event);
		}
	}

	// static helpers

	private static ThreadPoolExecutor createExecutor() {
		return new DBThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS);
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
}
