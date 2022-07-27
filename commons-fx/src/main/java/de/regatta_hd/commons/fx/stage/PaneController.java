package de.regatta_hd.commons.fx.stage;

import org.controlsfx.dialog.ProgressDialog;

import com.google.inject.Inject;

import de.regatta_hd.commons.db.DBConnection;
import de.regatta_hd.commons.fx.db.DBTask;
import de.regatta_hd.commons.fx.db.DBTaskRunner;
import de.regatta_hd.commons.fx.util.FxUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

public abstract class PaneController extends BaseController {

	@FXML
	private Pane rootPane;

	@Inject
	protected DBTaskRunner dbTaskRunner;
	@Inject
	protected DBConnection db;

	protected Window getWindow() {
		return this.rootPane.getScene().getWindow();
	}

	protected <T> void runTaskWithProgressDialog(DBTask<T> dbTask, String title, boolean cancel) {
		ProgressDialog dialog = FxUtils.showProgressDialog(getWindow(), title, cancel, dbTask);

		dbTask.setProgressMessageConsumer(t -> Platform.runLater(() -> dialog.setHeaderText(t)));
		this.dbTaskRunner.runTask(dbTask);
	}

	protected void setTitle(String title) {
		((Stage) getWindow()).setTitle(title);
	}

}
