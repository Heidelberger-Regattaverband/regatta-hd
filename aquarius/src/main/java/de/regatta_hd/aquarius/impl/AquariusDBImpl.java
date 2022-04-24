package de.regatta_hd.aquarius.impl;

import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private static final String DB_THREAD_PREFIX = "Database-Connection-";

	private final ThreadLocal<EntityManager> entityManager = new ThreadLocal<>();
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
			if (this.emFactory != null) {
				this.emFactory.close();
				this.emFactory = null;
			}
			if (this.dbExecutor != null) {
				this.dbExecutor.shutdownNow();
				this.dbExecutor = null;
			}
			this.entityManager.remove();
			this.version = null;

			// notify listeners about changed AquariusDB state
			notifyListeners(new AquariusDBStateChangedEventImpl(this));
		}
	}

	@Override
	public synchronized EntityManager getEntityManager() {
		checkIsOpen();
		if (this.entityManager.get() == null) {
			this.entityManager.set(this.emFactory.createEntityManager());
		}
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

	private void checkIsOpen() {
		if (!isOpenImpl()) {
			throw new IllegalStateException("Not connected.");
		}
		if (!Thread.currentThread().getName().startsWith(DB_THREAD_PREFIX)) {
			throw new IllegalThreadStateException("Not DB session thread.");
		}
	}

	private boolean isOpenImpl() {
		return this.emFactory != null && this.emFactory.isOpen();
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

	private ThreadPoolExecutor createExecutor() {
		return new DBThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
				new DatabaseThreadFactory());
	}

	// static helpers

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

	class DBThreadPoolExecutor extends ThreadPoolExecutor {
		private final Logger logger = Logger.getLogger(DBThreadPoolExecutor.class.getName());

		public DBThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			this.logger.log(Level.FINEST, "Before executing runnable in DBThreadPoolExecutor.");
			super.beforeExecute(t, r);
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			this.logger.log(Level.FINEST, "After executing runnable in DBThreadPoolExecutor.");
			super.afterExecute(r, t);
		}

		@Override
		protected void terminated() {
			super.terminated();
			this.logger.log(Level.FINEST, "Terminated DBThreadPoolExecutor.");
		}
	}

	private static class DatabaseThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable, DB_THREAD_PREFIX + poolNumber.getAndIncrement() + "-thread");
			thread.setDaemon(true);
			return thread;
		}
	}
}
