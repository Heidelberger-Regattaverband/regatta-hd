package de.regatta_hd.aquarius.db;

import java.io.IOException;

public interface ConnectionDataStore {

	ConnectionData getLastSuccessful() throws IOException;

	void setLastSuccessful(ConnectionData connectionData) throws IOException;
}
