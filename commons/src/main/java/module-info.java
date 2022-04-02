module de.regatta_hd.commons {

	// Java modules
	requires java.desktop;
	requires java.logging;

	// tools modules
	requires transitive com.google.guice;

	// exports
	exports de.regatta_hd.commons;
	exports de.regatta_hd.commons.concurrent;
	exports de.regatta_hd.commons.impl to de.regatta_hd.aquarius, com.google.guice;
}