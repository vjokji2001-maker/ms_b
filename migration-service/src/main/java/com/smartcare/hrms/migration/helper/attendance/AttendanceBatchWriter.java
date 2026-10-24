package com.smartcare.hrms.migration.helper.attendance;

import com.smartcare.hrms.migration.dto.attendance.AttendanceUploadRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class AttendanceBatchWriter {

    private @Autowired JdbcTemplate jdbcTemplate;

    // ─────────────────────────────────────────────────────────────────────────
    // Batch upsert attendance records
    //
    // emp_shift_id : UUID (String)
    // employee_id  : UUID (String)
    // ─────────────────────────────────────────────────────────────────────────
    public int batchUpsertAttendance(
            List<AttendanceUploadRequestDTO> rows,
            Map<String, String> codeToEmpId,
            Map<String, String> empDateToShiftId) {

        // All column names wrapped in backticks — `portion`, `status`, `in_time`, `out_time`
        // are reserved/semi-reserved words in MariaDB and must be escaped.
        String sql = "INSERT INTO `emp_atndnce` "
                + "(`emp_shift_id`, `employee_id`, `atndnce_date`,"
                + " `in_time`, `out_time`, `day_type`, `day_type1`, `portion`,"
                + " `late_in_mark`, `early_out_mark`, `nsa_amt`, `status`, `leave_rqst_id`, `nsa_days`) "
                + "VALUES (?, ?, ?, NULL, NULL, ?, NULL, ?, 0.0, 0.0, 0, 'PROCESED', NULL, 0) "
                + "ON DUPLICATE KEY UPDATE "
                + "  `day_type` = VALUES(`day_type`),"
                + "  `portion`  = VALUES(`portion`),"
                + "  `status`   = VALUES(`status`)";

        List<Object[]> batchArgs = new ArrayList<>();

        for (AttendanceUploadRequestDTO row : rows) {
            String empId = codeToEmpId.get(row.getEmployeeCode());
            YearMonth ym = YearMonth.of(row.getYear(), row.getMonth());
            List<LocalDate> dates = AttendanceDayDistributor.buildDateList(ym);
            Map<LocalDate, String> dateTypeMap = AttendanceDayDistributor.distributeDays(
                    dates, row.getPresentDays(), row.getAbsentDays(), row.getWeekOffDays());

            for (Map.Entry<LocalDate, String> entry : dateTypeMap.entrySet()) {
                LocalDate date       = entry.getKey();
                String dayType       = entry.getValue();
                String shiftKey      = empId + "_" + date;
                String empShiftIdStr = empDateToShiftId.get(shiftKey);

                if (empShiftIdStr == null) {
                    log.warn("No emp_shift_id for emp={} date={}, skipping", empId, date);
                    continue;
                }

                batchArgs.add(new Object[]{
                        Long.parseLong(empShiftIdStr), // emp_shift_id — bigint
                        empId,                         // employee_id  — char(36)
                        java.sql.Date.valueOf(date),   // atndnce_date
                        dayType,                       // day_type: DP / ABS / WO
                        1.0                            // portion
                });
            }
        }

        if (batchArgs.isEmpty()) {
            log.warn("No attendance rows to insert.");
            return 0;
        }

        jdbcTemplate.batchUpdate(sql, batchArgs, 500,
                (ps, args) -> {
                    ps.setLong(1,   (Long)          args[0]); // emp_shift_id
                    ps.setString(2, (String)        args[1]); // employee_id
                    ps.setDate(3,   (java.sql.Date) args[2]); // atndnce_date
                    ps.setString(4, (String)        args[3]); // day_type
                    ps.setDouble(5, (Double)        args[4]); // portion
                });

        log.info("Batch upserted {} attendance records", batchArgs.size());
        return batchArgs.size();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Batch upsert salary links — one per employee × month
    //
    // attendance_salary_link columns:
    //   id                char(36)  PK — UUID
    //   employee_id       char(36)     — UUID
    //   month_year        char(7)      — "yyyy-MM"
    //   night_shift_exist tinyint      — 0
    //   a_status          enum         — 'PENDING'
    //   p_status          enum         — 'PENDING'
    //   created_at        datetime     — NOW()
    //   updated_at        datetime     — NOW()
    //   nsa_amt / nsa_days             — default 0, not inserted
    // ─────────────────────────────────────────────────────────────────────────
    public int batchUpsertSalaryLinks(
            List<AttendanceUploadRequestDTO> rows,
            Map<String, String> codeToEmpId) {

        String sql = "INSERT INTO `attendance_salary_link` "
                + "(`id`, `employee_id`, `month_year`, `night_shift_exist`, `a_status`, `p_status`, `created_at`, `updated_at`) "
                + "VALUES (?, ?, ?, 0, 'PENDING', 'PENDING', NOW(), NOW()) "
                + "ON DUPLICATE KEY UPDATE "
                + "  `a_status`   = 'RECALCULATE',"
                + "  `updated_at` = NOW()";

        List<Object[]> batchArgs = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (AttendanceUploadRequestDTO row : rows) {
            String empId     = codeToEmpId.get(row.getEmployeeCode());
            String monthYear = YearMonth.of(row.getYear(), row.getMonth())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String key = empId + "_" + monthYear;

            if (seen.add(key)) {
                batchArgs.add(new Object[]{
                        UUID.randomUUID().toString(), // id
                        empId,                        // employee_id
                        monthYear                     // month_year
                });
            }
        }

        if (batchArgs.isEmpty()) return 0;

        jdbcTemplate.batchUpdate(sql, batchArgs, 500,
                (ps, args) -> {
                    ps.setString(1, (String) args[0]); // id
                    ps.setString(2, (String) args[1]); // employee_id
                    ps.setString(3, (String) args[2]); // month_year
                });

        log.info("Batch upserted {} salary links", batchArgs.size());
        return batchArgs.size();
    }
}
