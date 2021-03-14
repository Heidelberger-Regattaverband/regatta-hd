package de.regatta_hd.aquarius.db;

import java.io.IOException;

/**
 * A service to store {@link DBConfiguration database configuration} of last successful login.
 */
public interface DBConfigurationStore {

	DBConfiguration getLastSuccessful() throws IOException;

	void setLastSuccessful(DBConfiguration connectionData) throws IOException;
}
