module de.regatta_hd.ui {
	requires javafx.controls;
	requires javafx.fxml;

	opens de.regatta_hd.ui to javafx.fxml;

	exports de.regatta_hd.ui;
}