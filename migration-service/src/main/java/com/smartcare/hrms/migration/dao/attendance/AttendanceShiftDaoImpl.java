package com.smartcare.hrms.migration.dao.attendance;

import com.smartcare.hrms.migration.dto.attendance.AttendanceUploadRequestDTO;
import com.smartcare.hrms.migration.dto.attendance.EmployeeShiftDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AttendanceShiftDaoImpl {

    private @Autowired JdbcTemplate jdbcTemplate;

    private static final String GENERAL_SHIFT_NAME = "General Shift";
    private static final String GENERAL_SHIFT_IN   = "10:00:00";
    private static final String GENERAL_SHIFT_OUT  = "17:00:00";

    // ─────────────────────────────────────────────────────────────────────────
    // Find or create General Shift — returns its Long PK
    // ─────────────────────────────────────────────────────────────────────────
    public Long resolveGeneralShift() {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM shift_tmplt WHERE name = ? LIMIT 1",
                (rs, rn) -> rs.getLong("id"),
                GENERAL_SHIFT_NAME
        );

        if (!ids.isEmpty()) {
            log.info("General Shift found with id={}", ids.get(0));
            return ids.get(0);
        }

        // Not found — insert once
        jdbcTemplate.update(
                "INSERT INTO shift_tmplt (name, in_time, out_time, ttl_wrk_hr, night_shift, hour_base, half_day, cutoff_hr) "
                        + "VALUES (?, ?, ?, '07:00:00', 0, 0, 0, 3)",
                GENERAL_SHIFT_NAME, GENERAL_SHIFT_IN, GENERAL_SHIFT_OUT);

        Long newId = jdbcTemplate.queryForObject(
                "SELECT id FROM shift_tmplt WHERE name = ? LIMIT 1",
                Long.class,
                GENERAL_SHIFT_NAME);

        log.info("General Shift created with id={}", newId);
        return newId;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fetch employee UUIDs by codes — ONE query
    // Returns: emp_code → UUID String
    // ─────────────────────────────────────────────────────────────────────────
    public Map<String, String> fetchEmployeeIdsByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) return Collections.emptyMap();

        String placeholders = codes.stream().map(c -> "?").collect(Collectors.joining(","));
        String sql = "SELECT emp_code, id FROM emp_dtls WHERE emp_code IN (" + placeholders + ")";

        Map<String, String> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            result.put(rs.getString("emp_code"), rs.getString("id"));
        }, codes.toArray());

        log.info("Resolved {}/{} emp codes to UUIDs", result.size(), codes.size());
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fetch all emp_shift UUIDs for given employees + date range — ONE query
    //
    // Key  : "{empId}_{yyyy-MM-dd}"
    // Value : emp_shift.id (String — could be INT or UUID depending on your table)
    // ─────────────────────────────────────────────────────────────────────────
    public Map<String, String> fetchAllEmpShiftIds(
            List<AttendanceUploadRequestDTO> rows,
            Map<String, String> codeToEmpId) {

        Set<String> empIds = rows.stream()
                .map(r -> codeToEmpId.get(r.getEmployeeCode()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (empIds.isEmpty()) return Collections.emptyMap();

        LocalDate minDate = rows.stream()
                .map(r -> YearMonth.of(r.getYear(), r.getMonth()).atDay(1))
                .min(LocalDate::compareTo).orElseThrow();

        LocalDate maxDate = rows.stream()
                .map(r -> YearMonth.of(r.getYear(), r.getMonth()).atEndOfMonth())
                .max(LocalDate::compareTo).orElseThrow();

        String placeholders = empIds.stream().map(e -> "?").collect(Collectors.joining(","));
        String sql = "SELECT emp_id, id AS emp_shift_id, date"
                + " FROM emp_shift"
                + " WHERE emp_id IN (" + placeholders + ")"
                + " AND date BETWEEN ? AND ?";

        List<Object> params = new ArrayList<>(empIds);
        params.add(java.sql.Date.valueOf(minDate));
        params.add(java.sql.Date.valueOf(maxDate));

        Map<String, String> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            // id may be INT (1711) or UUID — getString handles both
            String key = rs.getString("emp_id") + "_" + rs.getDate("date").toLocalDate();
            result.put(key, rs.getString("emp_shift_id"));
        }, params.toArray());

        log.info("Fetched {} emp_shift records for date range {} → {}", result.size(), minDate, maxDate);
        return result;
    }
}
