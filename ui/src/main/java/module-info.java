open module de.regatta_hd.ui {
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;

	requires javax.inject;

	requires ignite.common;
	requires ignite.guice;
	requires com.google.guice;

	requires de.regatta_hd.aquarius.db;

	exports de.regatta_hd.ui;
}