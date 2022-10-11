module de.regatta_hd.ui {

	// JavaFX modules
	requires javafx.fxml;
	requires transitive javafx.graphics;

	requires org.apache.commons.lang3;
	requires org.apache.poi.poi;
	requires com.fazecast.jSerialComm;
	requires lombok;

	// regatta_hd modules
	requires de.regatta_hd.aquarius;
	requires de.regatta_hd.commons.core;
	requires de.regatta_hd.commons.db;
	requires de.regatta_hd.commons.fx;

	opens de.regatta_hd.ui to javafx.graphics, com.google.guice;
	opens de.regatta_hd.ui.util to javafx.fxml, com.google.guice, javafx.base;
	opens de.regatta_hd.ui.pane to javafx.fxml, com.google.guice;

	exports de.regatta_hd.ui;
}