module de.regatta_hd.mobile {

	requires com.gluonhq.charm.glisten;
	requires com.gluonhq.attach.display;
	requires com.gluonhq.attach.util;
	requires com.gluonhq.attach.storage;

	// regatta_hd modules
	requires de.regatta_hd.ui;

	opens de.regatta_hd.mobile to javafx.graphics;
}