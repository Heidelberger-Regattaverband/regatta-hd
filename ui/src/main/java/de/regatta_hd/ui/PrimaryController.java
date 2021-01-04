package de.regatta_hd.ui;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.AquariusDB;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

public class PrimaryController {

	@FXML
	private MenuBar menuBar;

	@Inject
	private AquariusDB aquarius;

	public void initialize() {
		Platform.runLater(() -> {
			if (!this.aquarius.isOpen()) {
				LoginDialog dialog = new LoginDialog((Stage) this.menuBar.getScene().getWindow(), true);
				dialog.show();
			}
		});
	}

	/**
	 * Handle action related to "About" menu item.
	 *
	 * @param event Event on "About" menu item.
	 */
	@FXML
	private void handleAboutAction(final ActionEvent event) {
		System.out.println("You clicked on About!");
	}
}
