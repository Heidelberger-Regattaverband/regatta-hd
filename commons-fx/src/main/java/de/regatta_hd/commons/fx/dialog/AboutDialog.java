package de.regatta_hd.commons.fx.dialog;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Window;

public class AboutDialog extends Dialog<Void> {

	public AboutDialog(Window owner, String title, String headerText, String text) {
		initModality(Modality.WINDOW_MODAL);
		initOwner(owner);
		setTitle(title);
		setHeaderText(headerText);
		setContentText(text);

		getDialogPane().getButtonTypes().addAll(ButtonType.OK);
	}
}
