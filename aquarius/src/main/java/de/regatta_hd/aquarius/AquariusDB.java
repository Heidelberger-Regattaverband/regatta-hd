package de.regatta_hd.aquarius;

import de.regatta_hd.commons.db.DBConnection;

public interface AquariusDB extends DBConnection {

	String getVersion();
}
