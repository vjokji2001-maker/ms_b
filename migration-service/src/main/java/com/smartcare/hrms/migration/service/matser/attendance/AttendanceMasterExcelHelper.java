package com.smartcare.hrms.migration.service.matser.attendance;

import com.smartcare.hrms.migration.dto.master.attendance.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AttendanceMasterExcelHelper {

    public static final String SHEET_HOLIDAY              = "Holiday";
    public static final String SHEET_HOLIDAY_DATES        = "HolidayDates";
    public static final String SHEET_SHIFT_TEMPLATE       = "ShiftTemplate";
    public static final String SHEET_WEEK_OFF             = "WeekOff";
    public static final String SHEET_WEEK_OFF_DETAILS     = "WeekOffDetails";
    public static final String SHEET_LEAVE_NAME        = "LeaveName";
    public static final String SHEET_LEAVE_TEMPLATE       = "LeaveTemplate";
    public static final String SHEET_LEAVE_TEMPLATE_DTLS  = "LeaveTemplateDtls";
    public static final String SHEET_APPROVAL_GROUP       = "ApprovalGroup";
    public static final String SHEET_APPROVAL_GROUP_DTLS  = "ApprovalGroupDtls";
    public static final String SHEET_MONTH_CYCLE          = "MonthCycle";
    public static final String SHEET_YEAR_CYCLE           = "YearCycle";

    public static boolean isValidExcelFile(MultipartFile file) {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                .equals(file.getContentType());
    }

    // ─── Parsers ──────────────────────────────────────────────────────────────────

    public static List<HolidayUploadDTO> parseHoliday(Workbook workbook) {
        List<HolidayUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_HOLIDAY);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(HolidayUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .year(fmt.formatCellValue(row.getCell(2)).trim())
                    .build());
        }
        return list;
    }

    public static List<HolidayDateUploadDTO> parseHolidayDates(Workbook workbook) {
        List<HolidayDateUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_HOLIDAY_DATES);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(HolidayDateUploadDTO.builder()
                    .holidayCode(fmt.formatCellValue(row.getCell(0)).trim())
                    .name(fmt.formatCellValue(row.getCell(1)).trim())
                    .date(fmt.formatCellValue(row.getCell(2)).trim())
                    .build());
        }
        return list;
    }

    public static List<ShiftTemplateUploadDTO> parseShiftTemplate(Workbook workbook) {
        List<ShiftTemplateUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_SHIFT_TEMPLATE);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(ShiftTemplateUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .nightShift(fmt.formatCellValue(row.getCell(2)).trim())
                    .halfDay(fmt.formatCellValue(row.getCell(3)).trim())
                    .hourBased(fmt.formatCellValue(row.getCell(4)).trim())
                    .inTime(fmt.formatCellValue(row.getCell(5)).trim())
                    .outTime(fmt.formatCellValue(row.getCell(6)).trim())
                    .totalWorkingHours(fmt.formatCellValue(row.getCell(7)).trim())
                    .firstHalfOutTime(fmt.formatCellValue(row.getCell(8)).trim())
                    .secondHalfInTime(fmt.formatCellValue(row.getCell(9)).trim())
                    .totalHalfDayHours(fmt.formatCellValue(row.getCell(10)).trim())
                    .bufferTime(fmt.formatCellValue(row.getCell(11)).trim())
                    .cutOffTime(fmt.formatCellValue(row.getCell(12)).trim())
                    .halfRate(fmt.formatCellValue(row.getCell(13)).trim())
                    .fullRate(fmt.formatCellValue(row.getCell(14)).trim())
                    .build());
        }
        return list;
    }

    public static List<WeekOffUploadDTO> parseWeekOff(Workbook workbook) {
        List<WeekOffUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_WEEK_OFF);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(WeekOffUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .build());
        }
        return list;
    }

    public static List<WeekOffDetailUploadDTO> parseWeekOffDetails(Workbook workbook) {
        List<WeekOffDetailUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_WEEK_OFF_DETAILS);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(WeekOffDetailUploadDTO.builder()
                    .weekOffCode(fmt.formatCellValue(row.getCell(0)).trim())
                    .day(fmt.formatCellValue(row.getCell(1)).trim())
                    .weekNo(fmt.formatCellValue(row.getCell(2)).trim())
                    .build());
        }
        return list;
    }
public static List<LeaveNameUploadDTO> parseLeaveName(Workbook workbook){
        List<LeaveNameUploadDTO> list= new ArrayList<>();
        Sheet sheet =workbook.getSheet(SHEET_LEAVE_NAME);
        if(sheet==null) return  list;
        DataFormatter fmt= new DataFormatter();
        Boolean isHeader= true;
        for(Row row:sheet){
            if(isHeader) { isHeader=false;continue;}
            if(isRowEmpty(row)) continue;
            list.add(LeaveNameUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .build());
        }
        return  list;
}
    public static List<LeaveTemplateUploadDTO> parseLeaveTemplate(Workbook workbook) {
        List<LeaveTemplateUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_LEAVE_TEMPLATE);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(LeaveTemplateUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .totalLeaveDays(fmt.formatCellValue(row.getCell(2)).trim())
                    .build());
        }
        return list;
    }

    public static List<LeaveTemplateDtlsUploadDTO> parseLeaveTemplateDtls(Workbook workbook) {
        List<LeaveTemplateDtlsUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_LEAVE_TEMPLATE_DTLS);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(LeaveTemplateDtlsUploadDTO.builder()
                    .leaveTemplateCode(fmt.formatCellValue(row.getCell(0)).trim())
                    .leaveNameCode(fmt.formatCellValue(row.getCell(1)).trim())
                    .yearlyCount(fmt.formatCellValue(row.getCell(2)).trim())
                    .quarterlyCount(fmt.formatCellValue(row.getCell(3)).trim())
                    .monthlyCount(fmt.formatCellValue(row.getCell(4)).trim())
                    .pastDays(fmt.formatCellValue(row.getCell(5)).trim())
                    .futureDays(fmt.formatCellValue(row.getCell(6)).trim())
                    .halfDayAllowed(fmt.formatCellValue(row.getCell(7)).trim())
                    .build());
        }
        return list;
    }

    public static List<ApprovalGroupUploadDTO> parseApprovalGroup(Workbook workbook) {
        List<ApprovalGroupUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_APPROVAL_GROUP);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(ApprovalGroupUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .build());
        }
        return list;
    }

    public static List<ApprovalGroupDtlsUploadDTO> parseApprovalGroupDtls(Workbook workbook) {
        List<ApprovalGroupDtlsUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_APPROVAL_GROUP_DTLS);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(ApprovalGroupDtlsUploadDTO.builder()
                    .approvalGroupCode(fmt.formatCellValue(row.getCell(0)).trim())
                    .empCode(fmt.formatCellValue(row.getCell(1)).trim())
                    .sequence(fmt.formatCellValue(row.getCell(2)).trim())
                    .build());
        }
        return list;
    }

    public static List<MonthCycleUploadDTO> parseMonthCycle(Workbook workbook) {
        List<MonthCycleUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_MONTH_CYCLE);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(MonthCycleUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .startDate(fmt.formatCellValue(row.getCell(2)).trim())
                    .endDate(fmt.formatCellValue(row.getCell(3)).trim())
                    .gradeCode(fmt.formatCellValue(row.getCell(4)).trim())
                    .build());
        }
        return list;
    }

    public static List<YearCycleUploadDTO> parseYearCycle(Workbook workbook) {
        List<YearCycleUploadDTO> list = new ArrayList<>();
        Sheet sheet = workbook.getSheet(SHEET_YEAR_CYCLE);
        if (sheet == null) return list;
        DataFormatter fmt = new DataFormatter();
        boolean isHeader = true;
        for (Row row : sheet) {
            if (isHeader) { isHeader = false; continue; }
            if (isRowEmpty(row)) continue;
            list.add(YearCycleUploadDTO.builder()
                    .name(fmt.formatCellValue(row.getCell(0)).trim())
                    .code(fmt.formatCellValue(row.getCell(1)).trim())
                    .fromMonthYear(fmt.formatCellValue(row.getCell(2)).trim())
                    .toMonthYear(fmt.formatCellValue(row.getCell(3)).trim())
                    .build());
        }
        return list;
    }

    // ─── Template Generator ───────────────────────────────────────────────────────

    public static byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            buildSheet(workbook, SHEET_HOLIDAY,
                    new String[]{"Holiday Name", "Holiday Code", "Holiday Year (YYYY)"},
                    new String[]{"Holiday 2024", "HOL2024", "2024"});

            buildSheet(workbook, SHEET_HOLIDAY_DATES,
                    new String[]{"Holiday Code (from Holiday sheet)", "Holiday Name", "Date (YYYY-MM-DD)"},
                    new String[]{"HOL2024", "Republic Day", "2024-01-26"});

            buildSheet(workbook, SHEET_SHIFT_TEMPLATE,
                    new String[]{
                            "Shift Template Name", "Shift Template Code", "Night Shift (true/false)", "Half Day (true/false)",
                            "Hour Based (true/false)", "In Time (HH:mm)", "Out Time (HH:mm)",
                            "Total Working Hours (HH:mm)", "First Half Out Time (HH:mm)",
                            "Second Half In Time (HH:mm)", "Total Half Day Hours (HH:mm)",
                            "Buffer Time (minutes)", "Cut Off Time (minutes)",
                            "Half Rate", "Full Rate"
                    },
                    new String[]{
                            "General Shift", "GEN", "false", "false", "false",
                            "09:00", "18:00", "09:00", "13:00", "14:00", "04:30",
                            "15", "30", "0.5", "1.0"
                    });

            buildSheet(workbook, SHEET_WEEK_OFF,
                    new String[]{"Week Off Name", "Week Off Code"},
                    new String[]{"Standard Week Off", "SWO"});

            buildSheet(workbook, SHEET_WEEK_OFF_DETAILS,
                    new String[]{
                            "Week Off Code (from WeekOff sheet)",
                            "Day (MONDAY/TUESDAY/WEDNESDAY/THURSDAY/FRIDAY/SATURDAY/SUNDAY)",
                            "Week No (FIRST/SECOND/THIRD/FOURTH/FIFTH/ALL)"
                    },
                    new String[]{"SWO", "SATURDAY", "ALL"});
            buildSheet(workbook, SHEET_LEAVE_NAME,
                    new  String[]{"Leave Name","Leave Code"},
                    new String[]{"Casual Leave ","CL01"});

            buildSheet(workbook, SHEET_LEAVE_TEMPLATE,
                    new String[]{"Leave Template Name", "Leave Template Code", "Total Leave Days"},
                    new String[]{"Standard Leave", "SL", "24.0"});

            buildSheet(workbook, SHEET_LEAVE_TEMPLATE_DTLS,
                    new String[]{
                            "Leave Template Code (from LeaveTemplate sheet)",
                            "Leave Name Code (from LeaveName master)",
                            "Yearly Count", "Quarterly Count", "Monthly Count",
                            "Past Days", "Future Days", "Half Day Allowed (true/false)"
                    },
                    new String[]{"SL", "CL", "12.0", "3.0", "1.0", "7.0", "30.0", "true"});

            buildSheet(workbook, SHEET_APPROVAL_GROUP,
                    new String[]{"Approval Name", "Approval Code"},
                    new String[]{"HR Approval Group", "HAG"});

            buildSheet(workbook, SHEET_APPROVAL_GROUP_DTLS,
                    new String[]{
                            "Approval Group Code (from ApprovalGroup sheet)",
                            "Employee Code",
                            "Sequence"
                    },
                    new String[]{"HAG", "EMP001", "1"});

            buildSheet(workbook, SHEET_MONTH_CYCLE,
                    new String[]{"Month Cycle Name", "Month Cycle Code", "Start Date (day of month)", "End Date (day of month)", "Grade Code"},
                    new String[]{"Standard Month Cycle", "SMC", "1", "31", "SR"});

            buildSheet(workbook, SHEET_YEAR_CYCLE,
                    new String[]{"Year Cycle Name", "Year Cycle Code", "From Month Year (YYYY-MM-DD)", "To Month Year (YYYY-MM-DD)"},
                    new String[]{"FY 2024-25", "FY2425", "2024-04-01", "2025-03-31"});

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ─── Sheet Builder ────────────────────────────────────────────────────────────

    private static void buildSheet(Workbook workbook, String sheetName, String[] headers, String[] sample) {
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);

        CellStyle sampleStyle = workbook.createCellStyle();
        sampleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        sampleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sampleStyle.setBorderBottom(BorderStyle.THIN);
        sampleStyle.setBorderTop(BorderStyle.THIN);
        sampleStyle.setBorderLeft(BorderStyle.THIN);
        sampleStyle.setBorderRight(BorderStyle.THIN);
        Font sampleFont = workbook.createFont();
        sampleFont.setItalic(true);
        sampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        sampleStyle.setFont(sampleFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 7500);
        }

        Row sampleRow = sheet.createRow(1);
        for (int i = 0; i < sample.length; i++) {
            Cell cell = sampleRow.createCell(i);
            cell.setCellValue(sample[i]);
            cell.setCellStyle(sampleStyle);
        }
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}

