package de.regatta_hd.commons.fx.stage;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.stage.Modality;
import javafx.stage.Stage;

public interface WindowManager {

	Stage newStage(URL resourceUrl, String title, ResourceBundle resources, Modality modality);

	void loadStage(Stage stage, URL resourceUrl, String title, ResourceBundle resources);
}
