package de.regatta_hd.aquarius;

import lombok.Builder;
import lombok.Data;

/**
 * Contains database connection configuration like user credentials and database name.
 */
@Data
@Builder
public class DBConfig {

	/**
	 * The database host name or IP address.
	 */
	private String dbHost;

	/**
	 * The database name in SQL Server.
	 */
	private String dbName;

	/**
	 * The user name credential.
	 */
	private String userName;

	/**
	 * The user's password.
	 */
	private String password;
}
