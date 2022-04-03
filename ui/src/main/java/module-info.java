module de.regatta_hd.ui {

	// JavaFX modules
	requires javafx.fxml;
	requires javafx.graphics;

	requires org.apache.commons.lang3;
	requires org.controlsfx.controls;
	requires org.apache.poi.poi;

	// regatta_hd modules
	requires de.regatta_hd.commons.core;
	requires de.regatta_hd.commons.db;
	requires de.regatta_hd.commons.fx;
	requires de.regatta_hd.aquarius;

	opens de.regatta_hd.ui to javafx.graphics, com.google.guice;
	opens de.regatta_hd.ui.util to javafx.fxml, com.google.guice;
	opens de.regatta_hd.ui.pane to javafx.fxml, com.google.guice;
}