open module de.regatta_hd.aquarius.db {
	requires java.base;

	requires com.google.guice;
	requires com.microsoft.sqlserver.jdbc;
	requires lombok;
	requires transitive jakarta.persistence;
	requires liquibase.core;
	requires org.hibernate.orm.core;

	requires de.regatta_hd.common;
	requires org.apache.commons.lang3;

	exports de.regatta_hd.aquarius.db;
	exports de.regatta_hd.aquarius.db.model;
}