package de.regatta_hd.commons.db;

import java.io.IOException;

/**
 * A service to store {@link DBConfig database configuration} of last
 * successful login.
 */
public interface DBConfigStore {

	/**
	 * @return the {@link DBConfig database configuration} of last successful
	 *         login or <code>null</code> if not available
	 * @throws IOException if an I/O error occurred during reading configuration
	 */
	DBConfig getLastSuccessful() throws IOException;

	/**
	 * Stores the given database configuration for later use.
	 * 
	 * @param connectionData the {@link DBConfig database configuration} to
	 *                       store
	 * @throws IOException if an I/O error occurred during writing configuration
	 */
	void setLastSuccessful(DBConfig connectionData) throws IOException;
}
