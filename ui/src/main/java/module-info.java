module de.regatta_hd.ui {
	requires javafx.controls;
	requires javafx.fxml;
	requires ignite.guice;
	requires com.google.guice;
	requires javax.inject;
	requires de.regatta_hd.aquarius.db;

	opens de.regatta_hd.ui to javafx.fxml,com.google.guice;

	exports de.regatta_hd.ui;
}