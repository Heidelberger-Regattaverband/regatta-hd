open module de.regatta_hd.ui {
	requires javafx.base;
	requires javafx.controls;
	requires transitive javafx.fxml;
	requires transitive javafx.graphics;

	requires java.persistence;
	requires java.sql;
	requires javax.inject;

	requires ignite.common;
	requires ignite.guice;
	requires transitive com.google.guice;
	requires com.microsoft.sqlserver.jdbc;
	requires org.hibernate.orm.core;

	requires transitive de.regatta_hd.aquarius.db;

	requires transitive jfxtras.window;

	exports de.regatta_hd.ui;
}