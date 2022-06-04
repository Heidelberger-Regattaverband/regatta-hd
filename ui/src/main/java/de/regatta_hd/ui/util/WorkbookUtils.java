package de.regatta_hd.ui.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

public class WorkbookUtils {

	private WorkbookUtils() {
		// avoid instances
	}

	public static CellStyle createHeaderCellStyle(Workbook workbook) {
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setFont(headerFont);

		return headerStyle;
	}

	public static void saveWorkbook(Workbook workbook, File file) throws IOException {
		try (FileOutputStream fileOut = new FileOutputStream(file)) {
			workbook.write(fileOut);
		}
	}

}
