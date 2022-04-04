module de.regatta_hd.commons.fx {

	// Java modules
	requires java.logging;
	requires java.prefs;

	// JavaFX modules
	requires transitive javafx.controls;
	requires transitive javafx.fxml;

	requires transitive de.regatta_hd.commons.db;

	// exports
	exports de.regatta_hd.commons.fx;
	exports de.regatta_hd.commons.fx.control;
	exports de.regatta_hd.commons.fx.db;
	exports de.regatta_hd.commons.fx.dialog;
	exports de.regatta_hd.commons.fx.guice;
	exports de.regatta_hd.commons.fx.util;

	opens de.regatta_hd.commons.fx.db to com.google.guice;
	opens de.regatta_hd.commons.fx.impl to com.google.guice;
}