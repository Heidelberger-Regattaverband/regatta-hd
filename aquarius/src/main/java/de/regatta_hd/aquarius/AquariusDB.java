package de.regatta_hd.aquarius;

import java.util.EventListener;
import java.util.concurrent.Executor;

import de.regatta_hd.commons.db.DBConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;

/**
 * This interface encapsulates access to Aquarius database.
 */
public interface AquariusDB extends DBConnection {

	/**
	 * Return an instance of <code>CriteriaBuilder</code> for the creation of <code>CriteriaQuery</code> objects.
	 *
	 * @return CriteriaBuilder instance
	 * @throws IllegalStateException if the entity manager has been closed
	 */
	CriteriaBuilder getCriteriaBuilder();

	/**
	 * @return {@link EntityManager} instance
	 */
	EntityManager getEntityManager();

	/**
	 * Returns the executor for DB tasks.
	 *
	 * @return an {@link Executor} for DB tasks.
	 */
	Executor getExecutor();

	interface StateChangedEvent {

		AquariusDB getAquariusDB();
	}

	/**
	 * An event listener interface for Aqurius DB state changed events.
	 */
	@FunctionalInterface
	interface StateChangedEventListener extends EventListener {

		void stateChanged(StateChangedEvent event);
	}

	void updateSchema();

}
