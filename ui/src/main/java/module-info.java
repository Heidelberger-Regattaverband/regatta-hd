open module de.regatta_hd.ui {
	requires javafx.base;
	requires javafx.controls;
	requires transitive javafx.fxml;
	requires transitive javafx.graphics;

	requires java.sql;

	requires transitive com.google.guice;
	requires com.microsoft.sqlserver.jdbc;
	requires org.hibernate.orm.core;

	requires transitive de.regatta_hd.aquarius.db;

	exports de.regatta_hd.ui;
}