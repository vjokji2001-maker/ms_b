package com.smartcare.hrms.migration.helper.attendance;

import com.smartcare.hrms.migration.dto.attendance.AttendanceUploadRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AttendanceExcelParser {

    private AttendanceExcelParser() {}

    public static List<AttendanceUploadRequestDTO> parse(InputStream is) throws Exception {
        List<AttendanceUploadRequestDTO> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                AttendanceUploadRequestDTO dto = new AttendanceUploadRequestDTO();
                dto.setEmployeeCode(getCellString(row, 0));
                dto.setMonth(getCellInt(row, 1));
                dto.setYear(getCellInt(row, 2));
                dto.setPresentDays(getCellInt(row, 3));
                dto.setAbsentDays(getCellInt(row, 4));
                dto.setWeekOffDays(getCellInt(row, 5));
                rows.add(dto);
            }
        }
        return rows;
    }

    private static String getCellString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private static Integer getCellInt(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        try {
            return (int) cell.getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isRowEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
