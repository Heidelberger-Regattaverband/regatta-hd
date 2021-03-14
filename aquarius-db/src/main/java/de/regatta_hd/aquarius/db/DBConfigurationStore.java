package de.regatta_hd.aquarius.db;

import java.io.IOException;

/**
 * A service to store {@link DBConfiguration database configuration} of last
 * successful login.
 */
public interface DBConfigurationStore {

	/**
	 * @return the {@link DBConfiguration database configuration} of last successful
	 *         login or <code>null</code> if not available
	 * @throws IOException if an I/O error occurred during reading configuration
	 */
	DBConfiguration getLastSuccessful() throws IOException;

	/**
	 * Stores the given database configuration for later use.
	 * 
	 * @param connectionData the {@link DBConfiguration database configuration} to
	 *                       store
	 * @throws IOException if an I/O error occurred during writing configuration
	 */
	void setLastSuccessful(DBConfiguration connectionData) throws IOException;
}
