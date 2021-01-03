module de.regatta_hd.aquarius.db {
	requires com.google.guice;
	requires javax.inject;
	requires java.persistence;
	requires java.base;

	opens de.regatta_hd.aquarius.db to com.google.guice;

	exports de.regatta_hd.aquarius.db;
}