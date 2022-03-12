package de.regatta_hd.ui.pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.ui.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class HeatsController extends AbstractRegattaDAOController {
	private static final String DELIMITER = ";";

	private static final Logger logger = Logger.getLogger(HeatsController.class.getName());

	@FXML
	private Button refreshBtn;
	@FXML
	private Button exportLightSystemBtn;
	@FXML
	private TableView<Heat> heatsTbl;
	@FXML
	private TableColumn<Heat, String> numberCol;

	private final ObservableList<Heat> heatsList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.heatsTbl.setItems(this.heatsList);
		this.heatsTbl.getSortOrder().add(this.numberCol);

		loadResults(false);
	}

	private void loadResults(boolean refresh) {
		disableButtons(true);
		this.heatsList.clear();

		this.dbTask.run(progress -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getHeats();
		}, dbResult -> {
			try {
				this.heatsList.setAll(dbResult.getResult());
				this.heatsTbl.sort();
				FxUtils.autoResizeColumns(this.heatsTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	@FXML
	void handleRefreshOnAction() {
		disableButtons(true);

		loadResults(true);

	}

	@FXML
	void handleExportLightSystemhOnAction() {
		disableButtons(true);

		createCsv();

		disableButtons(false);
	}

	private String createCsv() {
		StringBuilder builder = new StringBuilder(4096);
		builder.append("Index;RennNr;Abtlg;Delay Bahn 1;Delay Bahn 2;Delay Bahn 3;Delay Bahn 4;Boot Bahn 1;Boot Bahn 2;Boot Bahn 3;Boot Bahn 4;Status\n");

		for (Heat heat : this.heatsList) {
			builder.append(heat.getNumber()).append(DELIMITER);
			builder.append(heat.getRaceNumber()).append(DELIMITER);
		}

		return builder.toString();
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
	}

}
