package de.regatta_hd.aquarius.db;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

/**
 * This interface encapsulates access to Aquarius database.
 */
public interface AquariusDB {

	/**
	 * Closes connection to aquarius database.
	 */
	void close();

	/**
	 * Return an instance of <code>CriteriaBuilder</code> for the creation of
	 * <code>CriteriaQuery</code> objects.
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
	 * Indicates whether the connection to Aquarius database is open or not.
	 *
	 * @return <code>true</code> if connection to aquarius database is open,
	 *         otherwise <code>false</code>.
	 */
	boolean isOpen();

	/**
	 * Opens connection to Aquarius database.
	 *
	 * @param connectionData the {@link ConnectionData connection data}
	 */
	void open(ConnectionData connectionData);
}
