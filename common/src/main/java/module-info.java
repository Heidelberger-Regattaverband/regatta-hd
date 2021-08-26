module de.regatta_hd.common {
	requires com.google.guice;

	opens de.regatta_hd.common.impl to com.google.guice;

	exports de.regatta_hd.common;
}