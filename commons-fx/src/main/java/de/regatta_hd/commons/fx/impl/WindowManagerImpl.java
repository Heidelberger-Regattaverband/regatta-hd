package de.regatta_hd.commons.fx.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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

@Singleton
public class WindowManagerImpl implements WindowManager {
	private static final Logger logger = Logger.getLogger(WindowManagerImpl.class.getName());

	@Inject
	private FXMLLoaderFactory fxmlLoaderFactory;

	private final Map<URL, Stage> stages = new HashMap<>();

	@Override
	public Stage newStage(URL resourceUrl, String title, ResourceBundle resources) {
		Stage stage = this.stages.computeIfAbsent(resourceUrl, key -> {
			try {
				return newStageImpl(resourceUrl, title, resources);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				return null;
			}
		});
		if (stage != null) {
			stage.requestFocus();
		}
		return stage;

	}

	private Stage newStageImpl(URL resourceUrl, String title, ResourceBundle resources) throws IOException {
		FXMLLoader loader = this.fxmlLoaderFactory.newFXMLLoader();
		loader.setLocation(resourceUrl);
		loader.setResources(resources);
		Parent parent = loader.load();

		Stage stage = createStage(title, parent);

		FxUtils.loadSizeAndPos(resourceUrl.toString(), stage);
		stage.show();

		// When the stage closes store the current size and window location.
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
			FxUtils.storeSizeAndPos(resourceUrl.toString(), stage);
			this.stages.remove(resourceUrl);

			Controller controller = loader.getController();
			if (controller != null) {
				controller.shutdown();
			}
		});
		return stage;
	}

	private Stage createStage(String title, Parent parent) throws IOException {
		Stage stage = new Stage();
		stage.setTitle(title);
		stage.setScene(new Scene(parent));
		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("icon.png")) {
			stage.getIcons().add(new Image(in));
		}
		return stage;
	}
}
