package de.regatta_hd.commons.db;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.regatta_hd.commons.core.ListenerManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public abstract class AbstractDBConnection implements DBConnection {

	private final ThreadLocal<EntityManager> entityManager = ThreadLocal
			.withInitial(() -> this.emFactory.createEntityManager());

	private final ListenerManager listenerManager;

	private volatile ExecutorService executor; // NOSONAR

	protected volatile EntityManagerFactory emFactory;

	protected AbstractDBConnection(ListenerManager listenerManager) {
		this.listenerManager = requireNonNull(listenerManager, "listenerManager must not be null");
	}

	@Override
	public synchronized void close() {
		if (this.executor != null) {
			this.executor.shutdown();
			this.executor = null;
		}

		if (this.emFactory != null) {
			this.emFactory.close();
			this.emFactory = null;
		}

		if (this.entityManager != null) {
			this.entityManager.remove();
		}

		// notify listeners about changed AquariusDB state
		notifyListeners(new StateChangedEventImplementation());
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
