open module de.regatta_hd.ui {
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;

	requires java.persistence;
	requires java.sql;
	requires javax.inject;

	requires ignite.common;
	requires ignite.guice;
	requires com.google.guice;
	requires com.microsoft.sqlserver.jdbc;

	requires transitive de.regatta_hd.aquarius.db;

	exports de.regatta_hd.ui;
}