open module de.regatta_hd.ui {

	// Java modules
	requires java.sql;

	// JavaFX modules
	requires javafx.base;
	requires javafx.controls;
	requires transitive javafx.fxml;
	requires transitive javafx.graphics;

	requires transitive com.google.guice;
	requires com.microsoft.sqlserver.jdbc;
	requires org.hibernate.orm.core;
	requires org.apache.commons.lang3;
	requires lombok;
	requires org.controlsfx.controls;
	requires org.apache.poi.poi;

	// regatta_hd modules
	requires transitive de.regatta_hd.aquarius;
	requires de.regatta_hd.commons;
	requires de.regatta_hd.commons.fx;

	exports de.regatta_hd.ui;
}