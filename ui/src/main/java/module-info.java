open module de.regatta_hd.ui {
	requires javafx.base;
	requires javafx.controls;
	requires transitive javafx.fxml;
	requires transitive javafx.graphics;

	requires java.sql;

	requires transitive com.google.guice;
	requires com.microsoft.sqlserver.jdbc;
	requires org.hibernate.orm.core;

	requires transitive de.regatta_hd.aquarius;
	requires org.apache.commons.lang3;
	requires de.regatta_hd.common;
	requires lombok;
	requires java.prefs;
	requires org.controlsfx.controls;

	exports de.regatta_hd.ui;
}