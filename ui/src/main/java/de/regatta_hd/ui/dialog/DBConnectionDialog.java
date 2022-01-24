package de.regatta_hd.ui.dialog;

import java.util.Objects;
import java.util.ResourceBundle;

import de.regatta_hd.aquarius.DBConfig;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DBConnectionDialog extends Dialog<DBConfig> {

	private DBConfig connectionData;

	public DBConnectionDialog(Stage primaryStage, boolean decorated, ResourceBundle resources, DBConfig dbCfg) {
		initOwner(Objects.requireNonNull(primaryStage, "primaryStage"));
		this.connectionData = Objects.requireNonNullElse(dbCfg, DBConfig.builder().build());

		if (!decorated) {
			initStyle(StageStyle.UNDECORATED);
		}
		setHeaderText(resources.getString("DatabaseConnectionDialog.title"));

		GridPane gridpane = new GridPane();
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(5);
		gridpane.setVgap(5);

		Label dbHostLbl = new Label(resources.getString("DatabaseConnectionDialog.dbHost"));
		gridpane.add(dbHostLbl, 0, 1);
		Label dbNameLbl = new Label(resources.getString("DatabaseConnectionDialog.dbName"));
		gridpane.add(dbNameLbl, 0, 2);
		Label userNameLbl = new Label(resources.getString("DatabaseConnectionDialog.userName"));
		gridpane.add(userNameLbl, 0, 3);
		Label passwordLbl = new Label(resources.getString("DatabaseConnectionDialog.password"));
		gridpane.add(passwordLbl, 0, 4);

		CheckBox encryptCbox = new CheckBox(resources.getString("DatabaseConnectionDialog.encrypt"));
		encryptCbox.setSelected(dbCfg.isEncrypt());
		gridpane.add(encryptCbox, 0, 5);

		CheckBox trustServerCertificateCbox = new CheckBox(resources.getString("DatabaseConnectionDialog.trustServerCertificate"));
		trustServerCertificateCbox.setSelected(dbCfg.isTrustServerCertificate());
		trustServerCertificateCbox.setDisable(!encryptCbox.isSelected());
		gridpane.add(trustServerCertificateCbox, 0, 6);

		encryptCbox.addEventHandler(ActionEvent.ACTION, event -> trustServerCertificateCbox.setDisable(!encryptCbox.isSelected()));

		TextField hostNameFld = new TextField(this.connectionData.getDbHost());
		gridpane.add(hostNameFld, 1, 1);
		TextField dbNameFld = new TextField(this.connectionData.getDbName());
		gridpane.add(dbNameFld, 1, 2);
		TextField userNameFld = new TextField(this.connectionData.getUsername());
		gridpane.add(userNameFld, 1, 3);
		PasswordField passwordFld = new PasswordField();
		passwordFld.setText(this.connectionData.getPassword());
		gridpane.add(passwordFld, 1, 4);

		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		getDialogPane().setContent(gridpane);

		setResultConverter(dialogButton -> {
			if (dialogButton.getButtonData() == ButtonData.OK_DONE) {
				this.connectionData.setDbHost(hostNameFld.getText());
				this.connectionData.setDbName(dbNameFld.getText());
				this.connectionData.setUsername(userNameFld.getText());
				this.connectionData.setPassword(passwordFld.getText());
				this.connectionData.setEncrypt(encryptCbox.isSelected());
				this.connectionData.setTrustServerCertificate(trustServerCertificateCbox.isSelected());
				return this.connectionData;
			}
			return null;
		});
	}
}
