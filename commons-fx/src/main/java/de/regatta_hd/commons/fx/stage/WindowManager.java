package de.regatta_hd.commons.fx.stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public interface WindowManager {

	Stage newStage(URL resource, String title, ResourceBundle resources, Consumer<WindowEvent> closeHandler)
			throws IOException;
}
