package de.regatta_hd.commons.fx.dialog;

import static de.regatta_hd.commons.fx.util.FxUtils.bundle;

import java.util.Objects;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

import de.regatta_hd.commons.db.DBConfig;
import de.regatta_hd.commons.fx.util.FxUtils;

public class DBConnectionDialog extends Dialog<DBConfig> {

	private DBConfig connectionData;

	public DBConnectionDialog(Window window, DBConfig dbCfg) {

		initOwner(Objects.requireNonNull(window, "window"));
		this.connectionData = Objects.requireNonNullElse(dbCfg, DBConfig.builder().build());

		setHeaderText(bundle.getString("DatabaseConnectionDialog.title"));

		GridPane gridpane = new GridPane();
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(5);
		gridpane.setVgap(5);

		gridpane.getColumnConstraints().add(new ColumnConstraints());

		ColumnConstraints columnConstraints = new ColumnConstraints();
		columnConstraints.setFillWidth(true);
		columnConstraints.setHgrow(Priority.ALWAYS);
		gridpane.getColumnConstraints().add(columnConstraints);

		Label dbHostLbl = new Label(bundle.getString("DatabaseConnectionDialog.dbHost"));
		gridpane.add(dbHostLbl, 0, 1);
		Label dbNameLbl = new Label(bundle.getString("DatabaseConnectionDialog.dbName"));
		gridpane.add(dbNameLbl, 0, 2);
		Label userNameLbl = new Label(bundle.getString("DatabaseConnectionDialog.userName"));
		gridpane.add(userNameLbl, 0, 3);
		Label passwordLbl = new Label(bundle.getString("DatabaseConnectionDialog.password"));
		gridpane.add(passwordLbl, 0, 4);

		CheckBox updateSchemaCbox = new CheckBox(bundle.getString("DatabaseConnectionDialog.updateSchema"));
		updateSchemaCbox.setSelected(dbCfg.isUpdateSchema());
		gridpane.add(updateSchemaCbox, 0, 5);

		CheckBox encryptCbox = new CheckBox(bundle.getString("DatabaseConnectionDialog.encrypt"));
		encryptCbox.setSelected(dbCfg.isEncrypt());
		gridpane.add(encryptCbox, 0, 6);

		CheckBox trustServerCertificateCbox = new CheckBox(
				bundle.getString("DatabaseConnectionDialog.trustServerCertificate"));
		trustServerCertificateCbox.setSelected(dbCfg.isTrustServerCertificate());
		trustServerCertificateCbox.setDisable(!encryptCbox.isSelected());
		gridpane.add(trustServerCertificateCbox, 0, 7);

		encryptCbox.addEventHandler(ActionEvent.ACTION,
				event -> trustServerCertificateCbox.setDisable(!encryptCbox.isSelected()));

		TextField hostFld = new TextField(this.connectionData.getHost());
		gridpane.add(hostFld, 1, 1);
		TextField nameFld = new TextField(this.connectionData.getName());
		gridpane.add(nameFld, 1, 2);
		TextField userFld = new TextField(this.connectionData.getUser());
		gridpane.add(userFld, 1, 3);
		PasswordField passwordFld = new PasswordField();
		passwordFld.setText(this.connectionData.getPassword());
		gridpane.add(passwordFld, 1, 4);

		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		getDialogPane().setContent(gridpane);
		getDialogPane().setPrefWidth(FxUtils.DIALOG_WIDTH);

		setResultConverter(dialogButton -> {
			if (dialogButton.getButtonData() == ButtonData.OK_DONE) {
				this.connectionData.setHost(hostFld.getText());
				this.connectionData.setName(nameFld.getText());
				this.connectionData.setUser(userFld.getText());
				this.connectionData.setPassword(passwordFld.getText());
				this.connectionData.setUpdateSchema(updateSchemaCbox.isSelected());
				this.connectionData.setEncrypt(encryptCbox.isSelected());
				this.connectionData.setTrustServerCertificate(trustServerCertificateCbox.isSelected());
				return this.connectionData;
			}
			return null;
		});

		if (hostFld.getText() == null || hostFld.getText().isBlank()) {
			Platform.runLater(hostFld::requestFocus);
		} else if (nameFld.getText() == null || nameFld.getText().isBlank()) {
			Platform.runLater(nameFld::requestFocus);
		} else if (userFld.getText() == null || userFld.getText().isBlank()) {
			Platform.runLater(userFld::requestFocus);
		} else if (passwordFld.getText() == null || passwordFld.getText().isBlank()) {
			Platform.runLater(passwordFld::requestFocus);
		}
	}
}
