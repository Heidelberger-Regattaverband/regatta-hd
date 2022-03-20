package de.regatta_hd.ui.pane;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import org.controlsfx.dialog.ProgressDialog;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.AquariusDB;
import de.regatta_hd.ui.FXMLLoaderFactory;
import de.regatta_hd.ui.util.DBTask;
import de.regatta_hd.ui.util.DBTaskRunner;
import de.regatta_hd.ui.util.FxUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

abstract class AbstractBaseController implements Initializable {

	protected URL location;

	protected ResourceBundle resources;

	@Inject
	protected FXMLLoaderFactory fxmlLoaderFactory;
	@Inject
	protected DBTaskRunner dbTaskRunner;
	@Inject
	protected AquariusDB db;

	@FXML
	protected Pane rootPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.location = Objects.requireNonNull(location, "location");
		this.resources = Objects.requireNonNull(resources, "resources");
	}

	protected Stage newWindow(String resource, String title, Consumer<WindowEvent> closeHandler) throws IOException {
		FXMLLoader loader = this.fxmlLoaderFactory.newFXMLLoader();
		loader.setLocation(getClass().getResource(resource));
		loader.setResources(this.resources);
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
			closeHandler.accept(event);
		});

		return stage;
	}

	protected String getText(String key, Object... args) {
		String text = this.resources.getString(key);
		if (args.length > 0) {
			text = MessageFormat.format(text, args);
		}
		return text;
	}

	protected Window getWindow() {
		return this.rootPane.getScene().getWindow();
	}

	protected void setTitle(String title) {
		((Stage) getWindow()).setTitle(title);
	}

	protected void runTaskWithProgressDialog(DBTask<?> dbTask, String title) {
		ProgressDialog dialog = new ProgressDialog(dbTask);
		dialog.initOwner(getWindow());
		dialog.setTitle(title);
		dbTask.setProgressMessageConsumer(t -> Platform.runLater(() -> dialog.setHeaderText(t)));
		this.dbTaskRunner.runTask(dbTask);
	}

}
