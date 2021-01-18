package de.regatta_hd.aquarius.db;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionData {

	private String dbHost;

	private String dbName;

	private String userName;

	private String password;
}
