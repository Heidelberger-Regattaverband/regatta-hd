open module de.regatta_hd.aquarius {
	requires java.base;
	requires transitive javafx.base;

	requires com.google.guice;
	requires com.microsoft.sqlserver.jdbc;
	requires transitive jakarta.persistence;
	requires liquibase.core;
	requires lombok;
	requires org.apache.commons.lang3;
	requires org.hibernate.orm.core;

	requires de.regatta_hd.commons;

	exports de.regatta_hd.aquarius;
	exports de.regatta_hd.aquarius.model;
}