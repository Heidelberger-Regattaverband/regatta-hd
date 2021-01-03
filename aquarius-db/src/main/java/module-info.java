open module de.regatta_hd.aquarius.db {
	requires transitive java.persistence;
	requires java.base;
	requires javax.inject;
	
	requires com.google.guice;
	requires com.microsoft.sqlserver.jdbc;

	exports de.regatta_hd.aquarius.db;
	exports de.regatta_hd.aquarius.db.model;
}