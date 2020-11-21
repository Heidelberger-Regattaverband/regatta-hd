package de.regatta_hd.ui;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class PrimaryController {

	@FXML
	private MenuBar menuBar;


	/**
	 * Handle action related to "About" menu item.
	 *
	 * @param event Event on "About" menu item.
	 */
	@FXML
	private void handleAboutAction(final ActionEvent event) {
		provideAboutFunctionality();
	}

	/**
	 * Handle action related to input (in this case specifically only responds to keyboard event CTRL-A).
	 *
	 * @param event Input event.
	 */
	@FXML
	private void handleKeyInput(final InputEvent event) {
		if (event instanceof KeyEvent) {
			final KeyEvent keyEvent = (KeyEvent) event;
			if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A) {
				provideAboutFunctionality();
			}
		}
	}

	/**
	 * Perform functionality associated with "About" menu selection or CTRL-A.
	 */
	private void provideAboutFunctionality() {
		System.out.println("You clicked on About!");
	}

	@FXML
	private void switchToSecondary() throws IOException {
		App.setRoot("secondary");
	}
}
