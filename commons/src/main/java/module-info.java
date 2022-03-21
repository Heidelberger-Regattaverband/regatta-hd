open module de.regatta_hd.commons {

	requires com.google.guice;
	requires java.desktop;
	requires java.logging;

	exports de.regatta_hd.commons;
	exports de.regatta_hd.commons.concurrent;
	exports de.regatta_hd.commons.impl to de.regatta_hd.aquarius;
}