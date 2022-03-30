module de.regatta_hd.commons.db {

	requires transitive de.regatta_hd.commons;

	requires java.sql;

	// tools modules
	requires lombok;
	requires com.google.guice;

	// exports
	exports de.regatta_hd.commons.db;

	opens de.regatta_hd.commons.db.impl to com.google.guice;
}