package de.regatta_hd.commons.db;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.regatta_hd.commons.core.ListenerManager;

public abstract class AbstractDBConnection implements DBConnection {

	private volatile ExecutorService executor; // NOSONAR

	private final ListenerManager listenerManager;

	protected AbstractDBConnection(ListenerManager listenerManager) {
		this.listenerManager = requireNonNull(listenerManager, "listenerManager must not be null");
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

	@Override
	public synchronized void close() {
		if (this.executor != null) {
			this.executor.shutdownNow();
			this.executor = null;
		}
	}

	protected void ensureOpen() {
		if (!isOpenImpl()) {
			throw new IllegalStateException("Not connected.");
		}
		if (!Thread.currentThread().getName().startsWith(DBThreadPoolExecutor.DB_THREAD_PREFIX)) {
			throw new IllegalStateException("Not a Database connection thread.");
		}
	}

	protected abstract boolean isOpenImpl();

	protected abstract Map<String, String> getProperties(DBConfig dbConfig);

	protected void notifyListeners(StateChangedEvent event) {
		List<StateChangedEventListener> listeners = this.listenerManager.getListeners(StateChangedEventListener.class);
		for (StateChangedEventListener listener : listeners) {
			listener.stateChanged(event);
		}
	}

	// static helpers

	private static ThreadPoolExecutor createExecutor() {
		final int poolSize = 1;
		return new DBThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS);
	}

}
