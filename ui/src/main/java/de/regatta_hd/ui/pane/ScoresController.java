package de.regatta_hd.ui.pane;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import de.regatta_hd.aquarius.model.Regatta;
import de.regatta_hd.aquarius.model.Score;
import de.regatta_hd.commons.core.concurrent.ProgressMonitor;
import de.regatta_hd.commons.fx.db.DBTask;
import de.regatta_hd.commons.fx.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ScoresController extends AbstractRegattaDAOController {
	private static final Logger logger = Logger.getLogger(ScoresController.class.getName());

	@FXML
	private Button refreshBtn;
	@FXML
	private Button calculateBtn;
	@FXML
	private Button exportXslBtn;
	@FXML
	private TableView<Score> scoresTbl;
	@FXML
	private TableColumn<Score, Integer> rankCol;

	private final ObservableList<Score> scoresList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);

		this.scoresTbl.setItems(this.scoresList);
		this.scoresTbl.getSortOrder().add(this.rankCol);

		loadScores(false);
	}

	@Override
	protected void onActiveRegattaChanged(Regatta activeRegatta) {
		if (activeRegatta != null) {
			loadScores(true);
		} else {
			this.scoresList.clear();
			disableButtons(true);
		}
	}

	@Override
	protected String getTitle(Regatta activeRegatta) {
		return nonNull(activeRegatta) ? getText("PrimaryView.scoresMitm.text") + " - " + activeRegatta.getTitle()
				: getText("PrimaryView.scoresMitm.text");
	}

	@FXML
	void handleRefresh() {
		loadScores(true);
	}

	@FXML
	void handleCalculate() {
		disableButtons(true);
		updatePlaceholder(getText("common.loadData"));
		this.scoresList.clear();

		super.dbTaskRunner.runInTransaction(progress -> this.regattaDAO.calculateScores(), scores -> {
			try {
				this.scoresList.setAll(scores.getResult());
				this.scoresTbl.sort();
				FxUtils.autoResizeColumns(this.scoresTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				if (this.scoresList.isEmpty()) {
					updatePlaceholder(getText("common.noDataAvailable"));
				}
				disableButtons(false);
			}
		});
	}

	private void loadScores(boolean refresh) {
		disableButtons(true);
		updatePlaceholder(getText("common.loadData"));
		this.scoresList.clear();

		super.dbTaskRunner.run(progress -> {
			if (refresh) {
				super.db.getEntityManager().clear();
			}
			return this.regattaDAO.getScores();
		}, scores -> {
			try {
				this.scoresList.setAll(scores.getResult());
				this.scoresTbl.sort();
				FxUtils.autoResizeColumns(this.scoresTbl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				FxUtils.showErrorMessage(getWindow(), e);
			} finally {
				updatePlaceholder(getText("common.noDataAvailable"));
				disableButtons(false);
			}
		});
	}

	@FXML
	private void handleExportXslOnAction() {
		disableButtons(true);

		DBTask<Workbook> dbTask = super.dbTaskRunner.createTask(this::createXsl, dbResult -> {
			File file = FxUtils.showSaveDialog(getWindow(), getText("scores.xsl.fileName"),
					getText("scores.xsl.description"), "*.xls");
			if (file != null) {
				try (Workbook workbook = dbResult.getResult()) {
					saveXslFile(workbook, file);
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

		runTaskWithProgressDialog(dbTask, getText("scores.xsl.export"), false);

	}

	private Workbook createXsl(ProgressMonitor progress) {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Punktwertung");

		int rowIdx = 0;
		int cellIdx = 0;

		Font headerFont = workbook.createFont();
		headerFont.setBold(true);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setFont(headerFont);

		CellStyle pointsStyle = workbook.createCellStyle();
		pointsStyle.setDataFormat((short) 2);

		Row row = sheet.createRow(rowIdx++);
		Cell rankHeaderCell = row.createCell(cellIdx++);
		rankHeaderCell.setCellStyle(headerStyle);
		rankHeaderCell.setCellValue(getText("common.rank"));

		Cell pointsHeaderCell = row.createCell(cellIdx++);
		pointsHeaderCell.setCellStyle(headerStyle);
		pointsHeaderCell.setCellValue(getText("common.points"));

		Cell clubHeaderCell = row.createCell(cellIdx);
		clubHeaderCell.setCellStyle(headerStyle);
		clubHeaderCell.setCellValue(getText("common.club"));

		for (int j = 0; j < this.scoresList.size(); j++) {
			cellIdx = 0;

			Score heat = this.scoresList.get(j);
			row = sheet.createRow(rowIdx++);

			row.createCell(cellIdx++).setCellValue(heat.getRank());
			Cell pointsCell = row.createCell(cellIdx++);
			pointsCell.setCellStyle(pointsStyle);
			pointsCell.setCellValue(heat.getPoints());
			row.createCell(cellIdx).setCellValue(heat.getClubName());

			progress.update(j, this.scoresList.size(), getText("scores.xsl.progress", Short.valueOf(heat.getRank())));
		}
		return workbook;
	}

	private void saveXslFile(Workbook workbook, File file) {
		try (FileOutputStream fileOut = new FileOutputStream(file)) {
			workbook.write(fileOut);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			FxUtils.showErrorMessage(getWindow(), ex);
		}
	}

	private void updatePlaceholder(String text) {
		((Label) this.scoresTbl.getPlaceholder()).setText(text);
	}

	private void disableButtons(boolean disabled) {
		this.refreshBtn.setDisable(disabled);
		this.calculateBtn.setDisable(disabled);
		this.exportXslBtn.setDisable(disabled);
	}

}
