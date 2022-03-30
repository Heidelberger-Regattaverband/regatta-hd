package de.regatta_hd.commons.db;

import java.sql.SQLException;

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

}
