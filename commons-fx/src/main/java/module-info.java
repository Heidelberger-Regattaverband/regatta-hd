module de.regatta_hd.commons.fx {

	// Java modules
	requires java.logging;
	requires java.prefs;

	// JavaFX modules
	requires javafx.controls;

	// exports
	exports de.regatta_hd.commons.fx.control;
	exports de.regatta_hd.commons.fx.dialog;
	exports de.regatta_hd.commons.fx.util;
}