package com.smartcare.hrms.attendance.service.shift.scheduler;

import com.netflix.discovery.converters.Auto;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.common.dbconnection.ConnectionProvider;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class AutoShiftScheduler {

    private static final Calendar IST_CAL = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));

    private @Autowired EntityManager entityManager;
    private @Autowired MessageResource messageResource;
    private @Autowired ConnectionProvider connectionProvider;

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    public void assignDefaultShiftToEmployees() {

        LocalDate date = LocalDate.now(ZoneId.of("Asia/Kolkata")); // ✅ ONLY TODAY
        Timestamp tsDate = Timestamp.valueOf(date.atStartOfDay());

        System.out.println("Processing Date: " + date);

        try {
            Map<Object, Object> clients = connectionProvider.getAllActiveClients();

            for (Map.Entry<Object, Object> entry : clients.entrySet()) {

                String clinicId = entry.getKey().toString();
                DriverManagerDataSource ds = (DriverManagerDataSource) entry.getValue();

                try (Connection conn = ds.getConnection()) {

                    // 🔹 Load Default Shift
                    PreparedStatement defaultStmt = conn.prepareStatement(
                            "SELECT * FROM shift_tmplt WHERE name = 'Default Shift' LIMIT 1"
                    );
                    ResultSet rs = defaultStmt.executeQuery();

                    Map<String, Object> defaultShift = new HashMap<>();

                    if (rs.next()) {
                        defaultShift.put("id", rs.getLong("id"));
                        defaultShift.put("inTime", LocalTime.parse(rs.getString("in_time")));
                        defaultShift.put("outTime", LocalTime.parse(rs.getString("out_time")));

                        defaultShift.put("shInTime",
                                rs.getString("sh_in_time") == null ? null : LocalTime.parse(rs.getString("sh_in_time")));

                        defaultShift.put("fhOutTime",
                                rs.getString("fh_out_time") == null ? null : LocalTime.parse(rs.getString("fh_out_time")));

                        defaultShift.put("grace", rs.getInt("buffer_min"));
                        defaultShift.put("endBefore", rs.getInt("end_before"));
                    }

                    rs.close();
                    defaultStmt.close();

                    if (defaultShift.isEmpty()) {
                        System.out.println("No default shift for clinic: " + clinicId);
                        continue;
                    }

                    // 🔹 Load Employees
                    PreparedStatement empStmt = conn.prepareStatement("SELECT id FROM emp_dtls");
                    ResultSet empRs = empStmt.executeQuery();

                    List<String> employees = new ArrayList<>();
                    while (empRs.next()) employees.add(empRs.getString("id"));

                    empRs.close();
                    empStmt.close();

                    // 🔥 ONLY ONE DAY LOOP
                    for (String empId : employees) {

                        // 🔹 Check existing row
                        PreparedStatement checkStmt = conn.prepareStatement(
                                "SELECT id, shift_id FROM emp_shift WHERE emp_id=? AND date=?"
                        );
                        checkStmt.setString(1, empId);
                        checkStmt.setTimestamp(2, tsDate, IST_CAL);

                        ResultSet checkRs = checkStmt.executeQuery();
                        boolean exists = checkRs.next();
                        Long shiftIdExisting = exists ? checkRs.getLong("shift_id") : null;

                        checkRs.close();
                        checkStmt.close();

                        Map<String, Object> shift = defaultShift;
                        boolean isDefault = true;

                        // 🔹 Load existing shift
                        if (exists && shiftIdExisting != null) {

                            PreparedStatement st = conn.prepareStatement(
                                    "SELECT * FROM shift_tmplt WHERE id=?"
                            );
                            st.setLong(1, shiftIdExisting);

                            ResultSet srs = st.executeQuery();
                            if (srs.next()) {

                                shift = new HashMap<>();
                                shift.put("id", srs.getLong("id"));
                                shift.put("inTime", LocalTime.parse(srs.getString("in_time")));
                                shift.put("outTime", LocalTime.parse(srs.getString("out_time")));

                                shift.put("shInTime",
                                        srs.getString("sh_in_time") == null ? null : LocalTime.parse(srs.getString("sh_in_time")));

                                shift.put("fhOutTime",
                                        srs.getString("fh_out_time") == null ? null : LocalTime.parse(srs.getString("fh_out_time")));

                                shift.put("grace", srs.getInt("buffer_min"));
                                shift.put("endBefore", srs.getInt("end_before"));

                                isDefault = false;
                            }
                            srs.close();
                            st.close();
                        }

                        // 🔹 Extract values
                        Long shiftId = (Long) shift.get("id");
                        LocalTime inTime = (LocalTime) shift.get("inTime");
                        LocalTime outTime = (LocalTime) shift.get("outTime");
                        LocalTime fhOutTime = (LocalTime) shift.get("fhOutTime");
                        LocalTime shInTime = (LocalTime) shift.get("shInTime");
                        Integer grace = (Integer) shift.get("grace");
                        Integer endBefore = (Integer) shift.get("endBefore");

                        // 🔹 Date calculations
                        LocalDateTime shiftStart = LocalDateTime.of(date, inTime).minusMinutes(endBefore);
                        LocalDateTime shiftEnd = shiftStart.plusHours(23).plusMinutes(59);

                        LocalDateTime empIn = LocalDateTime.of(date, inTime);

                        LocalDateTime empOut = LocalDateTime.of(date, outTime);
                        if (!outTime.isAfter(inTime)) empOut = empOut.plusDays(1);

                        // 🔥 NULL handling
                        LocalDateTime fhOutDT = null;
                        if (fhOutTime != null) {
                            fhOutDT = LocalDateTime.of(date, fhOutTime);
                            if (!fhOutTime.isAfter(inTime)) fhOutDT = fhOutDT.plusDays(1);
                        }

                        LocalDateTime shInDT = null;
                        if (shInTime != null) {
                            shInDT = LocalDateTime.of(date, shInTime);
                            if (!shInTime.isAfter(inTime)) shInDT = shInDT.plusDays(1);
                        }

                        if (exists) {

                            PreparedStatement update = conn.prepareStatement(
                                    "UPDATE emp_shift SET shift_id=?, is_default=?, shift_strt_time=?, shift_end_time=?, " +
                                            "emp_in_time=?, emp_out_time=?, emp_fh_out_time=?, emp_sh_in_time=?, " +
                                            "cutoff_hr=?, grace_time_mins=? WHERE emp_id=? AND date=?"
                            );

                            update.setLong(1, shiftId);
                            update.setBoolean(2, isDefault);
                            update.setTimestamp(3, Timestamp.valueOf(shiftStart), IST_CAL);
                            update.setTimestamp(4, Timestamp.valueOf(shiftEnd), IST_CAL);
                            update.setTimestamp(5, Timestamp.valueOf(empIn), IST_CAL);
                            update.setTimestamp(6, Timestamp.valueOf(empOut), IST_CAL);

                            if (fhOutDT != null)
                                update.setTimestamp(7, Timestamp.valueOf(fhOutDT), IST_CAL);
                            else
                                update.setNull(7, Types.TIMESTAMP);

                            if (shInDT != null)
                                update.setTimestamp(8, Timestamp.valueOf(shInDT), IST_CAL);
                            else
                                update.setNull(8, Types.TIMESTAMP);

                            update.setInt(9, endBefore);
                            update.setInt(10, grace);
                            update.setString(11, empId);
                            update.setTimestamp(12, tsDate, IST_CAL);

                            update.executeUpdate();
                            update.close();

                        } else {

                            PreparedStatement insert = conn.prepareStatement(
                                    "INSERT INTO emp_shift (emp_id, shift_id, date, is_default, shift_strt_time, shift_end_time, " +
                                            "emp_in_time, emp_out_time, emp_fh_out_time, emp_sh_in_time, cutoff_hr, grace_time_mins, auto_adjust_time) " +
                                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"
                            );

                            insert.setString(1, empId);
                            insert.setLong(2, shiftId);
                            insert.setTimestamp(3, tsDate, IST_CAL);
                            insert.setBoolean(4, isDefault);
                            insert.setTimestamp(5, Timestamp.valueOf(shiftStart), IST_CAL);
                            insert.setTimestamp(6, Timestamp.valueOf(shiftEnd), IST_CAL);
                            insert.setTimestamp(7, Timestamp.valueOf(empIn), IST_CAL);
                            insert.setTimestamp(8, Timestamp.valueOf(empOut), IST_CAL);

                            if (fhOutDT != null)
                                insert.setTimestamp(9, Timestamp.valueOf(fhOutDT), IST_CAL);
                            else
                                insert.setNull(9, Types.TIMESTAMP);

                            if (shInDT != null)
                                insert.setTimestamp(10, Timestamp.valueOf(shInDT), IST_CAL);
                            else
                                insert.setNull(10, Types.TIMESTAMP);

                            insert.setInt(11, endBefore);
                            insert.setInt(12, grace);
                            insert.setInt(13, 0);

                            insert.executeUpdate();
                            insert.close();
                        }
                    }

                    System.out.println("Done for clinic: " + clinicId);

                } catch (Exception e) {
                    System.err.println("Error in clinic: " + clinicId);
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.err.println("Scheduler failed");
            e.printStackTrace();
        }
    }
}