package com.smartcare.hrms.migration.validator;

import com.smartcare.hrms.migration.dto.attendance.AttendanceUploadRequestDTO;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRowValidator {

    private AttendanceRowValidator() {}

    public static List<String> validate(List<AttendanceUploadRequestDTO> rows) {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            AttendanceUploadRequestDTO r = rows.get(i);
            int rowNum = i + 2; // Excel row number (1-indexed + 1 for header)

            if (r.getEmployeeCode() == null || r.getEmployeeCode().isBlank()) {
                errors.add("Row " + rowNum + ": Employee code is empty");
                continue;
            }
            if (r.getYear() == null || r.getMonth() == null) {
                errors.add("Row " + rowNum + ": Year or Month is missing");
                continue;
            }
            if (r.getMonth() < 1 || r.getMonth() > 12) {
                errors.add("Row " + rowNum + ": Invalid month " + r.getMonth());
                continue;
            }

            int totalDaysInMonth = YearMonth.of(r.getYear(), r.getMonth()).lengthOfMonth();
            int supplied = (r.getPresentDays() == null ? 0 : r.getPresentDays())
                    + (r.getAbsentDays()  == null ? 0 : r.getAbsentDays())
                    + (r.getWeekOffDays() == null ? 0 : r.getWeekOffDays());

            if (supplied != totalDaysInMonth) {
                errors.add("Row " + rowNum + " [" + r.getEmployeeCode() + "]: "
                        + "present(" + r.getPresentDays() + ") + absent(" + r.getAbsentDays()
                        + ") + weekOff(" + r.getWeekOffDays() + ") = " + supplied
                        + " but month has " + totalDaysInMonth + " days");
            }
        }
        return errors;
    }

    /**
     * Returns only the rows that do NOT appear in errorRows (matched by "Row N:" prefix).
     */
    public static List<AttendanceUploadRequestDTO> filterValid(
            List<AttendanceUploadRequestDTO> rows, List<String> errorRows) {

        List<AttendanceUploadRequestDTO> valid = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            final int rowNum = i + 2;
            boolean hasError = errorRows.stream().anyMatch(e -> e.startsWith("Row " + rowNum));
            if (!hasError) valid.add(rows.get(i));
        }
        return valid;
    }
}
