package de.regatta_hd.commons.fx.stage;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.stage.Stage;

public interface WindowManager {

	Stage newStage(URL resource, String title, ResourceBundle resources);
}
