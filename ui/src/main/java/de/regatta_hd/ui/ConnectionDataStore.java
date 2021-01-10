package de.regatta_hd.ui;

import java.io.IOException;

import de.regatta_hd.aquarius.db.ConnectionData;

public interface ConnectionDataStore {

	ConnectionData getLastSuccessful() throws IOException;

	void setLastSuccessful(ConnectionData connectionData) throws IOException;
}
