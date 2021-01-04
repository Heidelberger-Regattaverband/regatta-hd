package de.regatta_hd.ui;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginDialog extends Dialog<Integer> {

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
		Label userNameLbl = new Label("User Name: ");
		gridpane.add(userNameLbl, 0, 2);
		Label passwordLbl = new Label("Password: ");
		gridpane.add(passwordLbl, 0, 3);

		TextField hostNameFld = new TextField("localhost");
		gridpane.add(hostNameFld, 1, 1);
		TextField userNameFld = new TextField("Admin");
		gridpane.add(userNameFld, 1, 2);
		PasswordField passwordFld = new PasswordField();
		passwordFld.setText("password");
		gridpane.add(passwordFld, 1, 3);

		ButtonType okBtnType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
		ButtonType closeBtnType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

		getDialogPane().getButtonTypes().addAll(okBtnType, closeBtnType);
		getDialogPane().setContent(gridpane);
	}
}
