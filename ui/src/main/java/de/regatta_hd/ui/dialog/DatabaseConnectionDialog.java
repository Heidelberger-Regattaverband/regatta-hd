package de.regatta_hd.ui.dialog;

import java.util.Objects;
import java.util.ResourceBundle;

import de.regatta_hd.aquarius.db.ConnectionData;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DatabaseConnectionDialog extends Dialog<ConnectionData> {

	private ConnectionData connectionData;

	public DatabaseConnectionDialog(Stage primaryStage, boolean decorated, ResourceBundle resources,
			ConnectionData connectionData) {
		initOwner(Objects.requireNonNull(primaryStage, "primaryStage"));
		this.connectionData = Objects.requireNonNullElse(connectionData, ConnectionData.builder().build());

		if (!decorated) {
			initStyle(StageStyle.UNDECORATED);
		}
		setHeaderText(resources.getString("ConnectionDialog.title"));

		GridPane gridpane = new GridPane();
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(5);
		gridpane.setVgap(5);

		Label dbHostLbl = new Label(resources.getString("ConnectionDialog.dbHost"));
		gridpane.add(dbHostLbl, 0, 1);
		Label dbNameLbl = new Label(resources.getString("ConnectionDialog.dbName"));
		gridpane.add(dbNameLbl, 0, 2);
		Label userNameLbl = new Label(resources.getString("ConnectionDialog.userName"));
		gridpane.add(userNameLbl, 0, 3);
		Label passwordLbl = new Label(resources.getString("ConnectionDialog.password"));
		gridpane.add(passwordLbl, 0, 4);

		TextField hostNameFld = new TextField(this.connectionData.getDbHost());
		gridpane.add(hostNameFld, 1, 1);
		TextField dbNameFld = new TextField(this.connectionData.getDbName());
		gridpane.add(dbNameFld, 1, 2);
		TextField userNameFld = new TextField(this.connectionData.getUserName());
		gridpane.add(userNameFld, 1, 3);
		PasswordField passwordFld = new PasswordField();
		passwordFld.setText(this.connectionData.getPassword());
		gridpane.add(passwordFld, 1, 4);

		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		getDialogPane().setContent(gridpane);

		setResultConverter((dialogButton) -> {
			if (dialogButton.getButtonData() == ButtonData.OK_DONE) {
				this.connectionData.setDbHost(hostNameFld.getText());
				this.connectionData.setDbName(dbNameFld.getText());
				this.connectionData.setUserName(userNameFld.getText());
				this.connectionData.setPassword(passwordFld.getText());
				return this.connectionData;
			}
			return null;
		});
	}
}
