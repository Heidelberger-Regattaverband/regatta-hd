open module de.regatta_hd.common {
	requires com.google.guice;
	requires java.desktop;

	exports de.regatta_hd.common;
	exports de.regatta_hd.common.impl to de.regatta_hd.aquarius;
}