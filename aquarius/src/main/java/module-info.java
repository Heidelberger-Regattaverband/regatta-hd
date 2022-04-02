open module de.regatta_hd.aquarius {

	// JavaFX modules
	requires transitive javafx.base;

	// JDBC / JPA modules
	requires com.microsoft.sqlserver.jdbc;
	requires org.hibernate.orm.core;
	requires liquibase.core;

	// tools modules
	requires lombok;
	requires com.google.guice;
	requires org.apache.commons.lang3;

	requires de.regatta_hd.commons.db;

	// exports
	exports de.regatta_hd.aquarius;
	exports de.regatta_hd.aquarius.model;
}