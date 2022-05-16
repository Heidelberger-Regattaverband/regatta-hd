package de.regatta_hd.commons.db;

import java.sql.SQLException;
import java.util.EventListener;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import jakarta.persistence.EntityManager;

public interface DBConnection {

	/**
	 * Closes connection to aquarius database.
	 */
	void close();

	/**
	 * Indicates whether the connection to Aquarius database is open or not.
	 *
	 * @return <code>true</code> if connection to aquarius database is open, otherwise <code>false</code>.
	 */
	boolean isOpen();

	/**
	 * Opens connection to Aquarius database.
	 *
	 * @param connectionData the {@link DBConfig connection data}
	 * @throws SQLException
	 */
	void open(DBConfig connectionData) throws SQLException;

	<R> Future<R> execute(DBCallable<R> callable);

	/**
	 * Returns the executor for DB tasks.
	 *
	 * @return an {@link Executor} for DB tasks.
	 * @deprecated Use {@link #execute(DBCallable)} instead
	 */
	@Deprecated(since = "0.1.21")
	ExecutorService getExecutor();

	interface StateChangedEvent {

		DBConnection getDBConnection();
	}

	/**
	 * An event listener interface for Aqurius DB state changed events.
	 */
	@FunctionalInterface
	interface StateChangedEventListener extends EventListener {

		void stateChanged(StateChangedEvent event);
	}

	void updateSchema();

	/**
	 * @return {@link EntityManager} instance
	 * @deprecated Use {@link #execute(DBCallable)} instead
	 */
	@Deprecated(since = "0.1.21")
	EntityManager getEntityManager();

	@FunctionalInterface
	interface DBCallable<R> {

		R execute(EntityManager entityManager) throws Exception; // NOSONAR
	}
}
