package de.regatta_hd.commons.fx.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import com.google.inject.Inject;

import de.regatta_hd.commons.fx.guice.FXMLLoaderFactory;
import de.regatta_hd.commons.fx.stage.Controller;
import de.regatta_hd.commons.fx.stage.WindowManager;
import de.regatta_hd.commons.fx.util.FxUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class WindowManagerImpl implements WindowManager {

	@Inject
	private FXMLLoaderFactory fxmlLoaderFactory;

	@Override
	public Stage newStage(String resource, String title, ResourceBundle resources, Consumer<WindowEvent> closeHandler)
			throws IOException {
		FXMLLoader loader = this.fxmlLoaderFactory.newFXMLLoader();
		loader.setLocation(getClass().getResource(resource));
		loader.setResources(resources);
		Parent parent = loader.load();

		Stage stage = new Stage();
		stage.setTitle(title);
		stage.setScene(new Scene(parent));
		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("icon.png")) {
			stage.getIcons().add(new Image(in));
		}

		FxUtils.loadSizeAndPos(resource, stage);
		stage.show();

		// When the stage closes store the current size and window location.
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
			FxUtils.storeSizeAndPos(resource, stage);
			if (closeHandler != null) {
				closeHandler.accept(event);
			}
			Controller controller = loader.getController();
			controller.shutdown();
		});

		return stage;
	}

}
