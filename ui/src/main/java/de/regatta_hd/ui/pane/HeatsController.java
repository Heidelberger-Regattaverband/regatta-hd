package de.regatta_hd.ui.pane;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class HeatsController extends AbstractRegattaDAOController {
	private static final String DELIMITER = ";";

	private static final Logger logger = Logger.getLogger(HeatsController.class.getName());

	private static final Pattern delayPattern = Pattern.compile("\\d*[\\.,]?\\d+");

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

		this.dbTask.run(progress -> {
			return createCsv(this.heatsList);
		}, dbResult -> {
			try {
				File file = FxUtils.showSaveDialog(this.refreshBtn.getScene().getWindow(),
						getText("heats.csv.description"), "*.csv");
				if (file != null) {
					saveTextToFile(dbResult.getResult(), file);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(e);
			} finally {
				disableButtons(false);
			}
		});
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.exportBtn.setDisable(disabled);
	}

	// static helpers

	private static String createCsv(List<Heat> heats) {
		StringBuilder builder = new StringBuilder(4096);
		addCsvHeader(builder);

		for (Heat heat : heats) {
			builder.append(heat.getNumber()).append(DELIMITER);
			builder.append(heat.getRaceNumber()).append(DELIMITER);
			builder.append(heat.getDevisionNumber()).append(DELIMITER);

			List<HeatRegistration> heatRegs = heat.getEntriesSortedByLane();
			short laneCount = heat.getRace().getRaceMode().getLaneCount();
			short diff = (short) (laneCount - heatRegs.size());

			// add delays
			if (heat.getRace().getAgeClass().isMasters()) {
				for (HeatRegistration heatReg : heatRegs) {
					String comment = heatReg.getRegistration().getComment();
					if (StringUtils.isNotBlank(comment)) {
						String delay = "0";
						Matcher matcher = delayPattern.matcher(comment);
						if (matcher.find()) {
							delay = matcher.group();
						}
						builder.append(delay).append(DELIMITER);
					} else {
						builder.append("0").append(DELIMITER);
					}
				}
				for (int i = 0; i < diff; i++) {
					builder.append("0").append(DELIMITER);
				}
			} else {
				for (int i = 0; i < laneCount; i++) {
					builder.append("0").append(DELIMITER);
				}
			}

			// add bibs
			for (HeatRegistration heatReg : heatRegs) {
				builder.append(heatReg.getRegistration().getBib()).append(DELIMITER);
			}
			for (int i = 0; i < diff; i++) {
				builder.append("0").append(DELIMITER);
			}

			// add status
			builder.append("-").append(StringUtils.LF);
		}

		return builder.toString();
	}

	private static void addCsvHeader(StringBuilder builder) {
		builder.append("Index").append(DELIMITER).append("RennNr").append(DELIMITER).append("Abtlg").append(DELIMITER)
				.append("Delay Bahn 1").append(DELIMITER).append("Delay Bahn 2").append(DELIMITER)
				.append("Delay Bahn 3").append(DELIMITER).append("Delay Bahn 4").append(DELIMITER).append("Boot Bahn 1")
				.append(DELIMITER).append("Boot Bahn 2").append(DELIMITER).append("Boot Bahn 3").append(DELIMITER)
				.append("Boot Bahn 4").append(DELIMITER).append("Status").append(StringUtils.LF);
	}

	private static void saveTextToFile(String csvContent, File file) {
		try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
			writer.println(csvContent);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
			FxUtils.showErrorMessage(ex);
		}
	}

}
