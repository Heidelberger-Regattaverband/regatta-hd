open module de.regatta_hd.aquarius {

	// Java modules
	requires java.base;

	// JavaFX modules
	requires transitive javafx.base;

	// JDBC / JPA modules
	requires com.microsoft.sqlserver.jdbc;
	requires transitive jakarta.persistence;
	requires org.hibernate.orm.core;
	requires liquibase.core;

	// tools modules
	requires com.google.guice;
	requires lombok;
	requires org.apache.commons.lang3;

	requires de.regatta_hd.commons.db;

	// exports
	exports de.regatta_hd.aquarius;
	exports de.regatta_hd.aquarius.model;
}