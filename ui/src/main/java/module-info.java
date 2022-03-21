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
	requires de.regatta_hd.commons;
	requires de.regatta_hd.commons.fx;
	requires lombok;

	requires org.controlsfx.controls;
	requires org.apache.poi.poi;

	exports de.regatta_hd.ui;
}