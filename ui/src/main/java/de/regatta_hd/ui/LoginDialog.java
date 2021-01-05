package de.regatta_hd.ui;

import de.regatta_hd.aquarius.db.ConnectionData;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginDialog extends Dialog<ConnectionData> {

	public LoginDialog(Stage primaryStage, boolean decorated) {
		initOwner(primaryStage);
		if (!decorated) {
			initStyle(StageStyle.UNDECORATED);
		}

		GridPane gridpane = new GridPane();
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(5);
		gridpane.setVgap(5);

		Label hostNameLbl = new Label("Host Name: ");
		gridpane.add(hostNameLbl, 0, 1);
		Label dbNameLbl = new Label("Database Name: ");
		gridpane.add(dbNameLbl, 0, 2);
		Label userNameLbl = new Label("User Name: ");
		gridpane.add(userNameLbl, 0, 3);
		Label passwordLbl = new Label("Password: ");
		gridpane.add(passwordLbl, 0, 4);

		TextField hostNameFld = new TextField("192.168.0.130");
		gridpane.add(hostNameFld, 1, 1);
		TextField dbNameFld = new TextField("rudern");
		gridpane.add(dbNameFld, 1, 2);
		TextField userNameFld = new TextField("sa");
		gridpane.add(userNameFld, 1, 3);
		PasswordField passwordFld = new PasswordField();
		passwordFld.setText("regatta");
		gridpane.add(passwordFld, 1, 4);

		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		getDialogPane().setContent(gridpane);

		setResultConverter((dialogButton) -> {
			ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
			return data == ButtonData.OK_DONE
					? ConnectionData.builder().hostName(hostNameFld.getText()).dbName(dbNameFld.getText())
							.userName(userNameFld.getText()).password(passwordFld.getText()).build()
					: null;
		});
	}
}
