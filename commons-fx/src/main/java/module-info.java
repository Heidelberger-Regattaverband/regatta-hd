module de.regatta_hd.commons.fx {

	// Java modules
	requires java.logging;
	requires java.prefs;

	// JavaFX modules
	requires transitive javafx.controls;
	requires transitive javafx.fxml;

	requires transitive com.google.guice;
	requires transitive de.regatta_hd.commons.db;

	// exports
	exports de.regatta_hd.commons.fx;
	exports de.regatta_hd.commons.fx.control;
	exports de.regatta_hd.commons.fx.dialog;
	exports de.regatta_hd.commons.fx.util;
}