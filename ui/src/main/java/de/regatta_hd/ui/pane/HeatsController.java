package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.commons.core.concurrent.ProgressMonitor;
import de.regatta_hd.commons.fx.db.DBTask;
import de.regatta_hd.commons.fx.util.FxUtils;
import de.regatta_hd.ui.util.WorkbookUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class HeatsController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(HeatsController.class.getName());

	private static final String HEADER_INDEX = "Index";
	private static final String HEADER_RENN_NR = "RennNr";
	private static final String HEADER_ABTEILUNG = "Abtlg";
	private static final String HEADER_STATUS = "Status";
	private static final String HEADER_BOOT_BAHN_4 = "Boot Bahn 4";
	private static final String HEADER_BOOT_BAHN_3 = "Boot Bahn 3";
	private static final String HEADER_BOOT_BAHN_2 = "Boot Bahn 2";
	private static final String HEADER_BOOT_BAHN_1 = "Boot Bahn 1";
	private static final String HEADER_DELAY_BAHN_4 = "Delay Bahn 4";
	private static final String HEADER_DELAY_BAHN_3 = "Delay Bahn 3";
	private static final String HEADER_DELAY_BAHN_2 = "Delay Bahn 2";
	private static final String HEADER_DELAY_BAHN_1 = "Delay Bahn 1";
	private static final String DELIMITER = ";";
	private static final Pattern delayPattern = Pattern.compile("\\d*[\\.,]?\\d+");

	@FXML
	private Button refreshBtn;
	@FXML
	private Button exportCsvBtn;
	@FXML
	private Button exportXslBtn;
	@FXML
	private TableView<Heat> heatsTbl;
	@FXML
	private TableColumn<Heat, Instant> timeCol;

	@FXML
	private TableView<HeatRegistration> scheduleTbl;

	private final ObservableList<Heat> heatsList = FXCollections.observableArrayList();
	private final ObservableList<HeatRegistration> scheduleList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.heatsTbl.setItems(this.heatsList);
		this.heatsTbl.getSortOrder().add(this.timeCol);
		this.scheduleTbl.setItems(this.scheduleList);

		this.heatsTbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				this.scheduleList.setAll(newSelection.getEntries());
			} else {
				this.scheduleList.clear();
			}
		});

		loadHeats(false);
	}

	@Override
	protected void onActiveRegattaChanged(Regatta activeRegatta) {
		if (activeRegatta != null) {
			loadHeats(true);
		} else {
			this.heatsList.clear();
			disableButtons(true);
		}
	}

	@FXML
	void handleRefreshOnAction() {
		loadHeats(true);
	}

	@FXML
	void handleExportCsvOnAction() {
		disableButtons(true);

		DBTask<String> dbTask = super.dbTaskRunner.createTask(this::createCsv, dbResult -> {
			File file = FxUtils.showSaveDialog(getWindow(), "startliste.csv", getText("heats.csv.description"),
					"*.csv");
			if (file != null) {
				try {
					saveCsvFile(dbResult.getResult(), file);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					disableButtons(false);
				}
			} else {
				disableButtons(false);
			}
		}, false);

		runTaskWithProgressDialog(dbTask, getText("heats.csv.export"), false);
	}

	@FXML
	void handleExportXslOnAction() {
		disableButtons(true);

		DBTask<Workbook> dbTask = super.dbTaskRunner.createTask(this::createWorkbook, dbResult -> {
			File file = FxUtils.showSaveDialog(getWindow(), "startliste.xls", getText("heats.xsl.description"),
					"*.xls");
			if (file != null) {
				try (Workbook workbook = dbResult.getResult()) {
					WorkbookUtils.saveWorkbook(workbook, file);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					FxUtils.showErrorMessage(getWindow(), e);
				} finally {
					disableButtons(false);
				}
			} else {
				disableButtons(false);
			}
		}, false);

		runTaskWithProgressDialog(dbTask, getText("heats.csv.export"), false);
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("heats.title") + " - " + activeRegatta.getTitle()
				: getText("heats.title");
	}

	private void loadHeats(boolean refresh) {
		disableButtons(true);
		updatePlaceholder(getText("common.loadData"));
		Heat selectedItem = this.heatsTbl.getSelectionModel().getSelectedItem();

		super.dbTaskRunner.run(progress -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getHeats();
		}, dbResult -> {
			try {
				this.heatsList.setAll(dbResult.getResult());
				this.heatsTbl.sort();
				this.heatsTbl.getSelectionModel().select(selectedItem);
				FxUtils.autoResizeColumns(this.heatsTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				updatePlaceholder(getText("common.noDataAvailable"));
				disableButtons(false);
			}
		});
	}

	private Workbook createWorkbook(ProgressMonitor progress) {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Startliste");

		Row row = sheet.createRow(0);
		addHeader(workbook, row);

		for (int j = 0; j < this.heatsList.size(); j++) {
			int cellIdx = 0;

			Heat heat = this.heatsList.get(j);
			row = sheet.createRow(j + 1);

			row.createCell(cellIdx++).setCellValue(heat.getNumber());
			row.createCell(cellIdx++).setCellValue(heat.getRaceNumber());
			row.createCell(cellIdx++).setCellValue(heat.getDevisionNumber());

			List<HeatRegistration> heatRegs = heat.getEntriesSortedByLane();
			short laneCount = heat.getRace().getRaceMode().getLaneCount();
			short diff = (short) (laneCount - heatRegs.size());

			// add delays
			if (heat.getRace().getAgeClass().isMasters()) {
				for (HeatRegistration heatReg : heatRegs) {
					row.createCell(cellIdx++).setCellValue(getDelay(heatReg));
				}
				for (int i = 0; i < diff; i++) {
					row.createCell(cellIdx++).setCellValue(0);
				}
			} else {
				for (int i = 0; i < laneCount; i++) {
					row.createCell(cellIdx++).setCellValue(0);
				}
			}

			// add bibs
			for (HeatRegistration heatReg : heatRegs) {
				row.createCell(cellIdx++)
						.setCellValue(heatReg.getRegistration().getBib() != null
								? heatReg.getRegistration().getBib().doubleValue()
								: 0);
			}
			for (int i = 0; i < diff; i++) {
				row.createCell(cellIdx++).setCellValue(0);
			}

			// add status
			row.createCell(cellIdx).setCellValue("-");

			progress.update(j, this.heatsList.size(), getText("heats.csv.progress", Short.valueOf(heat.getNumber())));
		}
		return workbook;
	}

	private String createCsv(ProgressMonitor progress) {
		StringBuilder builder = new StringBuilder(4096);

		// add header line
		addCsvHeader(builder);

		for (int j = 0; j < this.heatsList.size(); j++) {
			Heat heat = this.heatsList.get(j);

			builder.append(heat.getNumber()).append(DELIMITER);
			builder.append(heat.getRaceNumber()).append(DELIMITER);
			builder.append(heat.getDevisionNumber()).append(DELIMITER);

			List<HeatRegistration> heatRegs = heat.getEntriesSortedByLane();
			short laneCount = heat.getRace().getRaceMode().getLaneCount();
			short diff = (short) (laneCount - heatRegs.size());

			// add delays
			if (heat.getRace().getAgeClass().isMasters()) {
				for (HeatRegistration heatReg : heatRegs) {
					builder.append(getDelay(heatReg)).append(DELIMITER);
				}
				for (int i = 0; i < diff; i++) {
					builder.append(0).append(DELIMITER);
				}
			} else {
				for (int i = 0; i < laneCount; i++) {
					builder.append(0).append(DELIMITER);
				}
			}

			// add bibs
			for (HeatRegistration heatReg : heatRegs) {
				builder.append(heatReg.getRegistration().getBib()).append(DELIMITER);
			}
			for (int i = 0; i < diff; i++) {
				builder.append(0).append(DELIMITER);
			}

			// add status
			builder.append("-").append(StringUtils.LF);

			progress.update(j, this.heatsList.size(), getText("heats.csv.progress", Short.valueOf(heat.getNumber())));
		}

		return builder.toString();
	}

	private void saveCsvFile(String csvContent, File file) {
		try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
			writer.println(csvContent);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			FxUtils.showErrorMessage(getWindow(), ex);
		}
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.exportCsvBtn.setDisable(disabled);
		this.exportXslBtn.setDisable(disabled);
		this.heatsTbl.setDisable(disabled);
		this.scheduleTbl.setDisable(disabled);
	}

	private void updatePlaceholder(String text) {
		((Label) this.heatsTbl.getPlaceholder()).setText(text);
	}

	// static helpers

	private static float getDelay(HeatRegistration heatReg) {
		float delay = 0;
		String comment = heatReg.getRegistration().getComment();
		if (StringUtils.isNotBlank(comment)) {
			Matcher matcher = delayPattern.matcher(comment);
			if (matcher.find()) {
				String delayStr = matcher.group().replace(",", ".");
				try {
					delay = Float.parseFloat(delayStr);
				} catch (NumberFormatException e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		return delay;
	}

	private static void addHeader(Workbook workbook, Row row) {
		int idx = 0;
		CellStyle headerCellStyle = WorkbookUtils.createHeaderCellStyle(workbook);

		Cell headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_INDEX);
		headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_RENN_NR);
		headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_ABTEILUNG);
		headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_DELAY_BAHN_1);
		headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_DELAY_BAHN_2);
		headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_DELAY_BAHN_3);
		headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_DELAY_BAHN_4);
		headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_BOOT_BAHN_1);
		headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_BOOT_BAHN_2);
		headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_BOOT_BAHN_3);
		headerCell = row.createCell(idx++);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_BOOT_BAHN_4);
		headerCell = row.createCell(idx);
		headerCell.setCellStyle(headerCellStyle);
		headerCell.setCellValue(HEADER_STATUS);
	}

	private static void addCsvHeader(StringBuilder builder) {
		builder.append(HEADER_INDEX).append(DELIMITER).append(HEADER_RENN_NR).append(DELIMITER).append(HEADER_ABTEILUNG)
				.append(DELIMITER).append(HEADER_DELAY_BAHN_1).append(DELIMITER).append(HEADER_DELAY_BAHN_2)
				.append(DELIMITER).append(HEADER_DELAY_BAHN_3).append(DELIMITER).append(HEADER_DELAY_BAHN_4)
				.append(DELIMITER).append(HEADER_BOOT_BAHN_1).append(DELIMITER).append(HEADER_BOOT_BAHN_2)
				.append(DELIMITER).append(HEADER_BOOT_BAHN_3).append(DELIMITER).append(HEADER_BOOT_BAHN_4)
				.append(DELIMITER).append(HEADER_STATUS).append(StringUtils.LF);
	}

}
