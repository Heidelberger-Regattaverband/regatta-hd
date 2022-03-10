package de.regatta_hd.ui.dialog;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Window;

public class AboutDialog extends Dialog<Void> {

	public AboutDialog(Window owner, ResourceBundle bundle, String version) {
		initModality(Modality.WINDOW_MODAL);
		initOwner(owner);
		setTitle(bundle.getString("about.title"));
		setHeaderText(bundle.getString("about.header"));
		setContentText(MessageFormat.format(bundle.getString("about.text"), version));

		getDialogPane().getButtonTypes().addAll(ButtonType.OK);
	}
}
