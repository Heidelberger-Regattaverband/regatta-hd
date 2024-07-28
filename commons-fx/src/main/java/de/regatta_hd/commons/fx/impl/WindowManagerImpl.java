package de.regatta_hd.commons.fx.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.regatta_hd.commons.fx.guice.FXMLLoaderFactory;
import de.regatta_hd.commons.fx.stage.Controller;
import de.regatta_hd.commons.fx.stage.WindowManager;
import de.regatta_hd.commons.fx.util.FxUtils;

@Singleton
public class WindowManagerImpl implements WindowManager {

	private final FXMLLoaderFactory fxmlLoaderFactory;

	private final Map<URL, Pair<Stage, Boolean>> stages = new HashMap<>(); // NOSONAR

	@Inject
	WindowManagerImpl(FXMLLoaderFactory fxmlLoaderFactory) {
		this.fxmlLoaderFactory = Objects.requireNonNull(fxmlLoaderFactory, "fxmlLoaderFactory must not be null");
	}

	@Override
	public void loadPrimaryStage(Stage primaryStage, URL fxmlResourceUrl, String title, ResourceBundle bundle) {
		this.stages.computeIfAbsent(fxmlResourceUrl, key -> {
			try {
				FXMLLoader loader = createFXMLLoader(fxmlResourceUrl, bundle);
				initializeStage(primaryStage, title, loader);
				FxUtils.loadSizeAndPos(fxmlResourceUrl.getPath(), primaryStage);
				primaryStage.show();
				return new Pair<>(primaryStage, Boolean.TRUE);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		});
		primaryStage.requestFocus();
	}

	@Override
	public Stage newStage(URL fxmlResourceUrl, String title, ResourceBundle bundle, String... styles) {
		return newStage(fxmlResourceUrl, title, bundle, null, styles);
	}

	@Override
	public Stage newStage(URL fxmlResourceUrl, String title, ResourceBundle bundle, Window owner, String... styles) {
		Pair<Stage, Boolean> stage = this.stages.computeIfAbsent(fxmlResourceUrl, key -> {
			try {
				Stage newStage = newStageImpl(fxmlResourceUrl, title, bundle, owner, styles);
				return new Pair<>(newStage, Boolean.FALSE);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		});
		stage.getKey().requestFocus();
		return stage.getKey();
	}

	private Stage newStageImpl(URL fxmlResourceUrl, String title, ResourceBundle bundle, Window owner, String... styles)
			throws IOException {
		FXMLLoader loader = createFXMLLoader(fxmlResourceUrl, bundle);

		Stage stage = new Stage();
		if (owner != null) {
			stage.initOwner(owner);
			stage.initModality(Modality.WINDOW_MODAL);
		}
		initializeStage(stage, title, loader, styles);
		FxUtils.loadSizeAndPos(fxmlResourceUrl.getPath(), stage);
		stage.show();

		return stage;
	}

	private FXMLLoader createFXMLLoader(URL fxmlResourceUrl, ResourceBundle bundle) {
		FXMLLoader loader = this.fxmlLoaderFactory.newFXMLLoader();
		loader.setLocation(fxmlResourceUrl);
		loader.setResources(bundle);
		return loader;
	}

	private void initializeStage(Stage stage, String title, FXMLLoader loader, String... styles) throws IOException {
		stage.setTitle(title);
		Scene scene = new Scene(loader.load());
		scene.getStylesheets().addAll(styles);
		stage.setScene(scene);
		try (InputStream in = WindowManager.class.getClassLoader().getResourceAsStream("icon.png")) {
			stage.getIcons().add(new Image(in));
		}

		// When the stage closes store the current size and window location.
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
			Controller controller = loader.getController();
			if (controller != null) {
				controller.shutdown();
			}

			URL location = loader.getLocation();
			FxUtils.storeSizeAndPos(location.getPath(), stage);

			Pair<Stage, Boolean> closedStage = this.stages.remove(location);

			// if primary window is closed, exit application
			if (closedStage.getValue().booleanValue()) {
				Platform.runLater(Platform::exit);
			}
		});
	}

}
