package de.regatta_hd.ui.pane;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import org.controlsfx.dialog.ProgressDialog;

import com.google.inject.Inject;

import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.fx.db.DBTask;
import de.regatta_hd.commons.fx.db.DBTaskRunner;
import de.regatta_hd.commons.fx.stage.Controller;
import de.regatta_hd.commons.fx.stage.WindowManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

abstract class AbstractBaseController implements Initializable, Controller {

	protected URL location;

	protected ResourceBundle resources;

	@Inject
	protected WindowManager windowManager;
	@Inject
	protected DBTaskRunner dbTaskRunner;
	@Inject
	protected DBConnection db;

	@FXML
	protected Pane rootPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.location = requireNonNull(location, "location must not be null");
		this.resources = requireNonNull(resources, "resources must not be null");
	}

	protected Stage newWindow(String resource, String title, Consumer<WindowEvent> closeHandler) throws IOException {
		return this.windowManager.newStage(getClass().getResource(resource), title, this.resources, closeHandler);
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

	protected void runTaskWithProgressDialog(DBTask<?> dbTask, String title) {
		ProgressDialog dialog = new ProgressDialog(dbTask);
		dialog.initOwner(getWindow());
		dialog.setTitle(title);
		dbTask.setProgressMessageConsumer(t -> Platform.runLater(() -> dialog.setHeaderText(t)));
		this.dbTaskRunner.runTask(dbTask);
	}

	protected void setTitle(String title) {
		((Stage) getWindow()).setTitle(title);
	}

}
