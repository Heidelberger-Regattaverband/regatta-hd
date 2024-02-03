package de.regatta_hd.commons.db;

import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

import de.regatta_hd.commons.core.ListenerManager;

public abstract class AbstractDBConnection implements DBConnection {

	private final ThreadLocal<EntityManager> entityManager = ThreadLocal
			.withInitial(() -> this.emFactory.createEntityManager());

	private final ListenerManager listenerManager;

	private final String persistenceUnitName;

	private volatile ExecutorService executor; // NOSONAR

	private volatile EntityManagerFactory emFactory; // NOSONAR

	private String name;

	protected AbstractDBConnection(String persistenceUnitName, ListenerManager listenerManager) {
		this.persistenceUnitName = requireNonNull(persistenceUnitName, "persistenceUnitName must not be null");
		this.listenerManager = requireNonNull(listenerManager, "listenerManager must not be null");
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public final synchronized void open(DBConfig dbConfig) throws SQLException {
		close();

		Map<String, String> props = getProperties(requireNonNull(dbConfig, "dbConfig must not be null"));

		try {
			this.emFactory = Persistence.createEntityManagerFactory(this.persistenceUnitName, props);

			openImpl();

			this.name = dbConfig.getName();

			// notify listeners about changed DB connection state
			notifyListeners(new StateChangedEventImplementation());
		} catch (PersistenceException pex) {
			close();
			convertException(pex);
		}
	}

	@Override
	public synchronized void close() {
		this.name = null;

		if (this.executor != null) {
			this.executor.shutdown();
			this.executor = null;
		}

		if (this.emFactory != null) {
			this.emFactory.close();
			this.emFactory = null;

			// notify listeners about changed DB connection state
			notifyListeners(new StateChangedEventImplementation());
		}

		if (this.entityManager != null) {
			this.entityManager.remove();
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
	public ExecutorService getExecutor() {
		if (this.executor == null) {
			synchronized (this) {
				if (this.executor == null) {
					this.executor = createExecutor();
				}
			}
		}
		return this.executor;
	}

	protected void ensureOpen() {
		if (!isOpenImpl()) {
			throw new IllegalStateException("Not connected.");
		}
		if (!Thread.currentThread().getName().startsWith(DBThreadPoolExecutor.DB_THREAD_PREFIX)) {
			throw new IllegalStateException("Not a Database connection thread.");
		}
	}

	protected abstract Map<String, String> getProperties(DBConfig dbConfig);

	protected abstract void openImpl();

	protected abstract void convertException(PersistenceException ex) throws SQLException;

	protected void notifyListeners(StateChangedEvent event) {
		List<StateChangedEventListener> listeners = this.listenerManager.getListeners(StateChangedEventListener.class);
		for (StateChangedEventListener listener : listeners) {
			listener.stateChanged(event);
		}
	}

	private boolean isOpenImpl() {
		return this.emFactory != null && this.emFactory.isOpen();
	}

	// static helpers

	private static ThreadPoolExecutor createExecutor() {
		final int poolSize = 1;
		return new DBThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS);
	}

	// internal classes

	private final class StateChangedEventImplementation implements StateChangedEvent {
		@Override
		public DBConnection getDBConnection() {
			return AbstractDBConnection.this;
		}
	}

}
