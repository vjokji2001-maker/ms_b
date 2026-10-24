package com.smartcare.hrms.attendance.service.attendance.scheduler;


import com.smartcare.hrms.common.dbconnection.ConnectionProvider;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;


@Service
public class AutoAttendanceScheduler {

    private @Autowired ConnectionProvider connectionProvider;
    private @Autowired EmployeeRepository employeeRepository;

    @Transactional
    @Scheduled(cron = "0 30 0 * * ?")
    public void assignDefaultAttendance() {


        LocalDate today = LocalDate.now();

        try {
            Map<Object, Object> clientDataSource =
                    connectionProvider.getAllActiveClients();

            for (Map.Entry<Object, Object> entry : clientDataSource.entrySet()) {

                String clinicId = entry.getKey().toString();
                DriverManagerDataSource dataSource =
                        (DriverManagerDataSource) entry.getValue();

                System.out.println("Scheduler started for clinic : " + clinicId);

                try (Connection connection = dataSource.getConnection()) {

                    connection.setAutoCommit(false);

                    /* Fetch shifts (skip clinic if table missing) */
                    PreparedStatement selectStmt;
                    try {
                        selectStmt = connection.prepareStatement("""
                            SELECT es.id AS emp_shift_id,
                                   es.emp_id
                            FROM emp_shift es
                            WHERE es.date = ?
                            AND NOT EXISTS (
                                SELECT 1
                                FROM emp_atndnce ea
                                WHERE ea.emp_shift_id = es.id
                                AND ea.atndnce_date = ?
                            )
                        """);
                    } catch (Exception e) {
                        System.out.println(" emp_shift table missing, skipping clinic : " + clinicId);
                        continue;
                    }

                    selectStmt.setDate(1, Date.valueOf(today));
                    selectStmt.setDate(2, Date.valueOf(today));

                    ResultSet rs = selectStmt.executeQuery();

                    /*  Attendance insert */
                    PreparedStatement insertStmt = connection.prepareStatement("""
                        INSERT INTO emp_atndnce
                        (emp_shift_id, employee_id, atndnce_date,
                         status, day_type, day_type1, `portion`)
                        VALUES (?, ?, ?, 'PENDING', ?, NULL, 1)
                    """);

                    int batchCount = 0;

                    while (rs.next()) {

                        Long empShiftId = rs.getLong("emp_shift_id");
                        String empId = rs.getString("emp_id");

                        /*  Week-off logic (PreparedStatement only) */
                        boolean isPH = isPublicHoliday(connection, empId, today);
                        boolean isWO = isWeekOff(connection, empId, today);

                        String dayType;
                        if (isPH) {
                            dayType = "PH";
                        } else if (isWO) {
                            dayType = "WO";
                        } else {
                            dayType = "DP";
                        }

                        insertStmt.setLong(1, empShiftId);
                        insertStmt.setString(2, empId);
                        insertStmt.setDate(3, Date.valueOf(today));
                        insertStmt.setString(4, dayType);

                        insertStmt.addBatch();
                        batchCount++;

                        if (batchCount % 500 == 0) {
                            insertStmt.executeBatch();
                        }
                    }

                    insertStmt.executeBatch();
                    connection.commit();

                    System.out.println("Attendance created for clinic : " + clinicId);

                } catch (Exception e) {
                    System.out.println(" Error in clinic : " + clinicId);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isPublicHoliday(Connection connection, String empId, LocalDate date) {

        try (PreparedStatement ps = connection.prepareStatement("""
            SELECT 1
            FROM emp_dtls ed
            JOIN emp_config ec ON ec.emp_id = ed.id
            JOIN holiday_prnt hp ON hp.id = ec.holiday_clndr_id
            JOIN holiday_chld hc ON hc.holiday_id = hp.id
            WHERE ed.id = ?
            AND hc.date = ?
        """)) {

            ps.setString(1, empId);
            ps.setDate(2, Date.valueOf(date));

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            return false;
        }
    }
    /* WEEK OFF LOGIC  */

    private boolean isWeekOff(
            Connection connection,
            String empId,
            LocalDate today) {

        try (PreparedStatement ps = connection.prepareStatement("""
            SELECT wd.week_no, wd.day
            FROM emp_dtls ed
            JOIN emp_config ec ON ec.emp_id = ed.id
            JOIN wk_off_prnt wp ON wp.id = ec.week_off_id
            JOIN wk_off_dtl wd ON wd.wk_off_id = wp.id
            WHERE ed.id = ?
        """)) {

            ps.setString(1, empId);
            ResultSet rs = ps.executeQuery();

            LocalDate monthStart = today.withDayOfMonth(1);

            while (rs.next()) {

                DayOfWeek dayOfWeek =
                        DayOfWeek.valueOf(rs.getString("day"));

                int weekIndex =
                        Integer.parseInt(
                                rs.getString("week_no").replace("WEEK_", "")
                        );

                Optional<LocalDate> weekOffDate =
                        getNthWeekdayOfMonth(
                                monthStart, dayOfWeek, weekIndex);

                if (weekOffDate.isPresent()
                        && weekOffDate.get().isEqual(today)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            // week-off tables missing → treat as working day
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*  DATE UTIL */

    private Optional<LocalDate> getNthWeekdayOfMonth(
            LocalDate month,
            DayOfWeek dayOfWeek,
            int nth) {

        LocalDate firstDay = month.withDayOfMonth(1);
        int count = 0;

        for (int i = 0; i < month.lengthOfMonth(); i++) {
            LocalDate date = firstDay.plusDays(i);
            if (date.getDayOfWeek() == dayOfWeek) {
                count++;
                if (count == nth) {
                    return Optional.of(date);
                }
            }
        }
        return Optional.empty();
    }
}