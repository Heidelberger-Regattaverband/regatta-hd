package de.regatta_hd.ui.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.ObservableList;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.inject.Inject;

import de.regatta_hd.aquarius.model.Heat;
import de.regatta_hd.aquarius.model.HeatRegistration;
import de.regatta_hd.commons.core.concurrent.ProgressMonitor;

public class TrafficLightsStartList {
	private static final Logger logger = Logger.getLogger(TrafficLightsStartList.class.getName());

	private static final Pattern delayPattern = Pattern.compile("[-+]?\\d*[\\.,]?\\d+");

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

	@Inject
	private ResourceBundle bundle;

	public String createCsv(ObservableList<Heat> heatsList, ProgressMonitor progress) {
		StringBuilder builder = new StringBuilder(4096);

		// add header line
		addCsvHeader(builder);

		for (int j = 0; j < heatsList.size(); j++) {
			Heat heat = heatsList.get(j);

			builder.append(heat.getNumber()).append(DELIMITER);
			builder.append(heat.getRaceNumber()).append(DELIMITER);
			builder.append(heat.getDivisionNumber()).append(DELIMITER);

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

			progress.update(j, heatsList.size(), getText("heats.csv.progress", Short.valueOf(heat.getNumber())));
		}

		return builder.toString();
	}

	public Workbook createWorkbook(ObservableList<Heat> heatsList, ProgressMonitor progress) {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Startliste");

		Row row = sheet.createRow(0);
		addHeader(workbook, row);

		for (int j = 0; j < heatsList.size(); j++) {
			int cellIdx = 0;

			Heat heat = heatsList.get(j);
			row = sheet.createRow(j + 1);

			row.createCell(cellIdx++).setCellValue(heat.getNumber());
			row.createCell(cellIdx++).setCellValue(heat.getRaceNumber());
			row.createCell(cellIdx++).setCellValue(heat.getDivisionNumber());

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

			progress.update(j, heatsList.size(), getText("heats.csv.progress", Short.valueOf(heat.getNumber())));
		}
		return workbook;
	}

	private String getText(String key, Object... args) {
		return MessageFormat.format(this.bundle.getString(key), args);
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
