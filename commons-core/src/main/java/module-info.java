module de.regatta_hd.commons.core {

	// Java modules
	requires java.desktop;
	requires java.logging;

	// tools modules
	requires transitive com.google.guice;

	// exports
	exports de.regatta_hd.commons.core;
	exports de.regatta_hd.commons.core.concurrent;
	exports de.regatta_hd.commons.core.impl to de.regatta_hd.aquarius, com.google.guice;
}