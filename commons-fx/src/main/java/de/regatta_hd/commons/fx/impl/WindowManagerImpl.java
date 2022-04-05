package de.regatta_hd.commons.fx.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.commons.fx.guice.FXMLLoaderFactory;
import de.regatta_hd.commons.fx.stage.Controller;
import de.regatta_hd.commons.fx.stage.WindowManager;
import de.regatta_hd.commons.fx.util.FxUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Singleton
public class WindowManagerImpl implements WindowManager {

	@Inject
	private FXMLLoaderFactory fxmlLoaderFactory;

	private final Map<URL, Stage> stages = new HashMap<>(); // NOSONAR

	@Override
	public Stage newStage(URL resourceUrl, String title, ResourceBundle resources) {
		Stage stage = this.stages.computeIfAbsent(resourceUrl, key -> {
			try {
				return newStageImpl(resourceUrl, title, resources);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		});
		stage.requestFocus();
		return stage;
	}

	@Override
	public void loadStage(Stage stage, URL resourceUrl, String title, ResourceBundle resources) {
		this.stages.computeIfAbsent(resourceUrl, key -> {
			try {
				loadStageImpl(stage, resourceUrl, title, resources);
				return stage;
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		});
		stage.requestFocus();
	}

	private Stage newStageImpl(URL resourceUrl, String title, ResourceBundle resources) throws IOException {
		FXMLLoader loader = createLoader(resourceUrl, resources);

		Stage stage = createStage(title, loader);

		FxUtils.loadSizeAndPos(resourceUrl.toString(), stage);
		stage.show();

		return stage;
	}

	private void loadStageImpl(Stage stage, URL resourceUrl, String title, ResourceBundle resources)
			throws IOException {
		FXMLLoader loader = createLoader(resourceUrl, resources);

		initializeStage(stage, title, loader);

		FxUtils.loadSizeAndPos(resourceUrl.toString(), stage);
		stage.show();
	}

	private FXMLLoader createLoader(URL resourceUrl, ResourceBundle resources) {
		FXMLLoader loader = this.fxmlLoaderFactory.newFXMLLoader();
		loader.setLocation(resourceUrl);
		loader.setResources(resources);
		return loader;
	}

	private Stage createStage(String title, FXMLLoader loader) throws IOException {
		Stage stage = new Stage();
		initializeStage(stage, title, loader);
		return stage;
	}

	private void initializeStage(Stage stage, String title, FXMLLoader loader) throws IOException {
		stage.setTitle(title);
		stage.setScene(new Scene(loader.load()));
		try (InputStream in = WindowManager.class.getClassLoader().getResourceAsStream("icon.png")) {
			stage.getIcons().add(new Image(in));
		}

		// When the stage closes store the current size and window location.
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
			URL location = loader.getLocation();
			FxUtils.storeSizeAndPos(location.toString(), stage);
			this.stages.remove(location);

			Controller controller = loader.getController();
			if (controller != null) {
				controller.shutdown();
			}
		});
	}

}
