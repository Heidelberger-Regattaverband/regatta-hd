package de.regatta_hd.ui.pane;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.controlsfx.dialog.ProgressDialog;

import com.google.inject.Inject;

import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.fx.db.DBTask;
import de.regatta_hd.commons.fx.db.DBTaskRunner;
import de.regatta_hd.commons.fx.stage.Controller;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

abstract class AbstractBaseController implements Initializable, Controller {

	protected URL location;

	protected ResourceBundle resources;

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

	protected void runTaskWithProgressDialog(DBTask<?> dbTask, String title, boolean cancel) {
		ProgressDialog dialog = new ProgressDialog(dbTask);
		dialog.initOwner(getWindow());
		dialog.setTitle(title);
		dialog.getDialogPane().setPrefWidth(400);
		if (cancel) {
			dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
			dialog.setOnCloseRequest(event -> {
				if (event.getEventType() == DialogEvent.DIALOG_CLOSE_REQUEST) {
					dbTask.cancel();
				}
			});
		}
		dbTask.setProgressMessageConsumer(t -> Platform.runLater(() -> dialog.setHeaderText(t)));
		this.dbTaskRunner.runTask(dbTask);
	}

	protected void setTitle(String title) {
		((Stage) getWindow()).setTitle(title);
	}

}
