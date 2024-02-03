package de.regatta_hd.commons.db;

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
	private String host;

	/**
	 * The database name in SQL Server.
	 */
	private String name;

	/**
	 * The user name credential.
	 */
	private String user;

	/**
	 * The user's password.
	 */
	private String password;

	/**
	 * Enables encryption of connection to database.
	 */
	private boolean encrypt;

	/**
	 * Trust server certificate without validation if encrypted connection is enabled.
	 */
	private boolean trustServerCertificate;

	/**
	 * Update schema of database.
	 */
	private boolean updateSchema;
}
