package com.smartcare.hrms.migration.service.attendance;

import com.smartcare.hrms.migration.dto.attendance.EmployeeShiftDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class EmployeeShiftUpdateService {

    private @Autowired JdbcTemplate jdbcTemplate;

    // ── General Shift defaults ────────────────────────────────────────────────
    // Mirrors: shiftDetailBean.getShiftInTime() / getShiftOutTime()
    private static final LocalTime SHIFT_IN_TIME   = LocalTime.of(9, 0);   // 09:00
    private static final LocalTime SHIFT_OUT_TIME  = LocalTime.of(17, 0);  // 17:00

    // Mirrors: shiftDetailBean.getCutoffHr() → default 3 hrs
    private static final long      CUTOFF_HOURS    = 3L;
    private static final BigDecimal CUTOFF_HR_VAL  = new BigDecimal("3.00");

    // Mirrors: shiftDetailBean.getGraceTimeMins()
    private static final int       GRACE_TIME_MINS = 8;

    // Mirrors: shiftDetailBean.getAutoAdjustTime()
    private static final String    AUTO_ADJUST_TIME = "00:00:00";

    // General Shift has no half-day → firstHalfOutTime / secondHalfInTime = NULL
    private static final LocalTime FIRST_HALF_OUT  = null;
    private static final LocalTime SECOND_HALF_IN  = null;

    /**
     * Batch inserts emp_shift rows, mirroring convertForCreate() exactly:
     *
     *   shiftStartTime  = date + inTime - cutoffHours     → 2026-05-01 06:00:00
     *   shiftEndTime    = shiftStart + 23hr 59min         → 2026-05-02 05:59:00
     *   empInTime       = date + inTime                   → 2026-05-01 09:00:00
     *   empOutTime      = toDateTimeAdjusted(outTime)     → 2026-05-01 17:00:00
     *   empFhOutTime    = NULL  (no half-day)
     *   empShInTime     = NULL  (no half-day)
     *
     * ON DUPLICATE KEY (emp_id, date): updates shift + time columns only.
     */
    public void update(List<EmployeeShiftDTO> shiftDTOs) {
        if (shiftDTOs == null || shiftDTOs.isEmpty()) {
            log.warn("No shift DTOs to process.");
            return;
        }

        String sql = "INSERT INTO emp_shift "
                + "( emp_id, shift_id, date, is_default,"
                + " shift_strt_time, emp_in_time, emp_fh_out_time, emp_sh_in_time, emp_out_time, shift_end_time,"
                + " cutoff_hr, grace_time_mins, auto_adjust_time) "
                + "VALUES ( ?, ?, ?, ?,"
                + "        ?, ?, NULL, NULL, ?, ?,"
                + "        ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "  shift_id        = VALUES(shift_id),"
                + "  is_default      = VALUES(is_default),"
                + "  shift_strt_time = VALUES(shift_strt_time),"
                + "  emp_in_time     = VALUES(emp_in_time),"
                + "  emp_out_time    = VALUES(emp_out_time),"
                + "  shift_end_time  = VALUES(shift_end_time),"
                + "  cutoff_hr       = VALUES(cutoff_hr),"
                + "  grace_time_mins = VALUES(grace_time_mins)";

        jdbcTemplate.batchUpdate(sql, shiftDTOs, 500, (ps, dto) -> {
            LocalDate date = LocalDate.parse(dto.getDate());

            // ── Exact mirror of convertForCreate() ───────────────────────────

            // shiftStartTime = date + inTime - cutoffHours
            LocalDateTime shiftStart = LocalDateTime.of(date, SHIFT_IN_TIME)
                    .minusHours(CUTOFF_HOURS);

            // shiftEndTime = shiftStart + 23hr 59min
            LocalDateTime shiftEnd   = shiftStart.plusHours(23).plusMinutes(59);

            // empInTime = date + inTime  (straight, no adjustment)
            LocalDateTime empIn      = LocalDateTime.of(date, SHIFT_IN_TIME);

            // empOutTime = toDateTimeAdjusted(date, outTime, inTime, outTime)
            // outTime(17:00) >= inTime(09:00) → same date
            LocalDateTime empOut     = toDateTimeAdjusted(date, SHIFT_OUT_TIME,
                    SHIFT_IN_TIME, SHIFT_OUT_TIME);

            // empFhOutTime = toDateTimeAdjusted(date, firstHalfOutTime, ...) → NULL for General Shift
            // empShInTime  = toDateTimeAdjusted(date, secondHalfInTime, ...) → NULL for General Shift

            ps.setString(1,     dto.getEmpId());                 // emp_id   — UUID String
            ps.setLong(2,       dto.getShiftId());               // shift_id — Long
            ps.setDate(3,       java.sql.Date.valueOf(date));    // date
            ps.setInt(4,        dto.getIsDefault());             // is_default (1)
            ps.setTimestamp(5,  Timestamp.valueOf(shiftStart));  // shift_strt_time  → 06:00
            ps.setTimestamp(6,  Timestamp.valueOf(empIn));       // emp_in_time      → 09:00
            // col 8 = emp_fh_out_time → NULL (hardcoded in SQL)
            // col 9 = emp_sh_in_time  → NULL (hardcoded in SQL)
            ps.setTimestamp(7,  Timestamp.valueOf(empOut));      // emp_out_time     → 17:00
            ps.setTimestamp(8,  Timestamp.valueOf(shiftEnd));    // shift_end_time   → next day 05:59
            ps.setBigDecimal(9, CUTOFF_HR_VAL);                 // cutoff_hr        → 3.00
            ps.setInt(10,       GRACE_TIME_MINS);                // grace_time_mins  → 8
            ps.setString(11,    AUTO_ADJUST_TIME);               // auto_adjust_time → 00:00:00
        });

        log.info("Batch upserted {} emp_shift rows", shiftDTOs.size());
    }

    /**
     * Exact copy of toDateTimeAdjusted() from your existing shift service.
     *
     * Rule:
     *   targetTime >= inTime  → same date        (normal day shift)
     *   targetTime < inTime   → next day         (overnight shift)
     *
     * For General Shift: outTime(17:00) >= inTime(09:00) → always same date.
     */
    private static LocalDateTime toDateTimeAdjusted(
            LocalDate date,
            LocalTime targetTime,
            LocalTime inTime,
            LocalTime outTime) {

        if (targetTime == null) return null;

        if (targetTime.isBefore(inTime)) {
            return LocalDateTime.of(date.plusDays(1), targetTime);
        }
        return LocalDateTime.of(date, targetTime);
    }
}
