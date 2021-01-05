package de.regatta_hd.ui;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.regatta_hd.aquarius.db.AquariusDB;
import de.regatta_hd.aquarius.db.ConnectionData;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

public class PrimaryController implements Initializable{

	@FXML
	private MenuBar menuBar;

	@Inject
	private AquariusDB aquarius;

	/**
	 * Handle action related to "About" menu item.
	 *
	 * @param event Event on "About" menu item.
	 */
	@FXML
	private void handleAboutAction(final ActionEvent event) {
		System.out.println("You clicked on About!");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(() -> {
			if (!this.aquarius.isOpen()) {
				DatabaseConnectionDialog dialog = new DatabaseConnectionDialog((Stage) this.menuBar.getScene().getWindow(), true, resources);
				Optional<ConnectionData> result = dialog.showAndWait();
				if (result.isPresent()) {
					this.aquarius.open(result.get());
				}
			}
		});
	}
}
