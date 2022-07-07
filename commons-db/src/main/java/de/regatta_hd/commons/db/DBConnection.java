package de.regatta_hd.commons.db;

import java.sql.SQLException;
import java.util.EventListener;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import jakarta.persistence.EntityManager;

public interface DBConnection {

	/**
	 * Returns the name of the database.
	 *
	 * @return the database name or {@code null} if not connected.
	 */
	String getName();

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

	/**
	 * Returns the executor for DB tasks.
	 *
	 * @return an {@link Executor} for DB tasks.
	 */
	ExecutorService getExecutor();

	void updateSchema();

	/**
	 * @return {@link EntityManager} instance
	 */
	EntityManager getEntityManager();

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

}
