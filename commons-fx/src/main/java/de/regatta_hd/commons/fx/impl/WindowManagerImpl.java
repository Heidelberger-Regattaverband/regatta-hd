package de.regatta_hd.commons.fx.impl;

import static java.util.Objects.requireNonNull;

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
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Singleton
public class WindowManagerImpl implements WindowManager {
	private static final Logger logger = Logger.getLogger(WindowManagerImpl.class.getName());

	@Inject
	private FXMLLoaderFactory fxmlLoaderFactory;

	private final Map<URL, Stage> stages = new HashMap<>(); // NOSONAR

	@Override
	public Stage newStage(URL resourceUrl, String title, ResourceBundle resources, Modality modality) {
		Stage stage = this.stages.computeIfAbsent(resourceUrl, key -> {
			try {
				return newStageImpl(resourceUrl, title, resources, modality);
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

	private Stage newStageImpl(URL resourceUrl, String title, ResourceBundle resources, Modality modality)
			throws IOException {
		FXMLLoader fxmlLoader = newFxmlLoader(resourceUrl, resources);

		Stage stage = createStage(title, fxmlLoader);
		stage.initModality(modality != null ? modality : Modality.NONE);
		FxUtils.loadSizeAndPos(resourceUrl.getPath(), stage);
		stage.show();

		return stage;
	}

	private void loadStageImpl(Stage stage, URL resourceUrl, String title, ResourceBundle resources)
			throws IOException {
		FXMLLoader fxmlLoader = newFxmlLoader(resourceUrl, resources);

		initializeStage(stage, title, fxmlLoader);

		FxUtils.loadSizeAndPos(resourceUrl.getPath(), stage);
		stage.show();
	}

	private FXMLLoader newFxmlLoader(URL resourceUrl, ResourceBundle resources) {
		FXMLLoader loader = this.fxmlLoaderFactory.newFXMLLoader();
		loader.setLocation(requireNonNull(resourceUrl, "resourceUrl must not be null"));
		loader.setResources(requireNonNull(resources, "resources must not be null"));
		return loader;
	}

	private Stage createStage(String title, FXMLLoader fxmlLoader) throws IOException {
		Stage stage = new Stage();
		initializeStage(stage, title, fxmlLoader);
		return stage;
	}

	private void initializeStage(Stage stage, String title, FXMLLoader fxmlLoader) throws IOException {
		stage.setTitle(title);
		stage.setScene(new Scene(fxmlLoader.load()));
		try (InputStream in = WindowManager.class.getClassLoader().getResourceAsStream("icon.png")) {
			stage.getIcons().add(new Image(in));
		}

		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
			URL location = fxmlLoader.getLocation();

			// When the stage closes store the current size and window location.
			FxUtils.storeSizeAndPos(location.getPath(), stage);

			this.stages.remove(location);
			logger.log(Level.FINE, "Removed stage {0} from management.", location);

			Controller controller = fxmlLoader.getController();
			if (controller != null) {
				controller.shutdown();
			}
		});
	}

}
