package de.regatta_hd.ui.pane;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.ui.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

public class HeatsController extends AbstractRegattaDAOController {
	private static final String DELIMITER = ";";

	private static final Logger logger = Logger.getLogger(HeatsController.class.getName());

	@FXML
	private Button refreshBtn;
	@FXML
	private Button exportBtn;
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
	void handleExportOnAction() {
		disableButtons(true);

		String csvContent = createCsv();

		FileChooser fileChooser = new FileChooser();

		// Set extension filter for text files
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

		// Show save file dialog
		File file = fileChooser.showSaveDialog(this.refreshBtn.getScene().getWindow());

		if (file != null) {
			saveTextToFile(csvContent, file);
		}

		disableButtons(false);
	}

	private String createCsv() {
		StringBuilder builder = new StringBuilder(4096);
		builder.append("Index").append(DELIMITER).append("RennNr").append(DELIMITER).append("Abtlg").append(DELIMITER)
				.append("Delay Bahn 1").append(DELIMITER).append("Delay Bahn 2").append(DELIMITER)
				.append("Delay Bahn 3").append(DELIMITER).append("Delay Bahn 4").append(DELIMITER).append("Boot Bahn 1")
				.append(DELIMITER).append("Boot Bahn 2").append(DELIMITER).append("Boot Bahn 3").append(DELIMITER)
				.append("Boot Bahn 4").append(DELIMITER).append("Status").append(StringUtils.LF);

		for (Heat heat : this.heatsList) {
			builder.append(heat.getNumber()).append(DELIMITER);
			builder.append(heat.getRaceNumber()).append(DELIMITER);
			builder.append(heat.getDevisionNumber()).append(DELIMITER);
			builder.append("0").append(DELIMITER); // delay lane 1
			builder.append("0").append(DELIMITER); // delay lane 2
			builder.append("0").append(DELIMITER); // delay lane 3
			builder.append("0").append(DELIMITER); // delay lane 4

			short laneCount = heat.getRace().getRaceMode().getLaneCount();
			List<HeatRegistration> heatRegs = heat.getHeatRegistrationsOrderedByLane();
			for (HeatRegistration heatReg : heatRegs) {
				builder.append(heatReg.getRegistration().getBib()).append(DELIMITER);
			}
			short diff = (short) (laneCount - heatRegs.size());
			for (int i = 0; i < diff; i++) {
				builder.append("0").append(DELIMITER);
			}
			builder.append("-").append(StringUtils.LF);
		}

		return builder.toString();
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.exportBtn.setDisable(disabled);
	}

	// static helpers

	private static void saveTextToFile(String csvContent, File file) {
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.println(csvContent);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
			FxUtils.showErrorMessage(ex);
		}
	}

}
