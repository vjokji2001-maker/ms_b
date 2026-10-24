package com.smartcare.hrms.attendance.service.attendance.scheduler;

import com.smartcare.hrms.common.dbconnection.ConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Service
public class AutoAttendanceSalaryLinkScheduler {


    @Autowired
    private ConnectionProvider connectionProvider;

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void linkAttendanceToSalary() {

        // Current month
        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        try {
            Map<Object, Object> clientDataSource = connectionProvider.getAllActiveClients();

            for (Map.Entry<Object, Object> entry : clientDataSource.entrySet()) {

                String clinicId = entry.getKey().toString();
                DriverManagerDataSource dataSource = (DriverManagerDataSource) entry.getValue();

                try (Connection connection = dataSource.getConnection()) {

                    connection.setAutoCommit(false);

                    try (
                            // Fetch only employees who DON'T have a record for current month
                            PreparedStatement fetchEmpId = connection.prepareStatement(
                                    "SELECT id AS emp_id FROM emp_dtls " +
                                            "WHERE id NOT IN ( " +
                                            "   SELECT employee_id FROM attendance_salary_link " +
                                            "   WHERE month_year = ? " +
                                            ")"
                            )
                    ) {
                        fetchEmpId.setString(1, monthYear);

                        try (
                                ResultSet rs = fetchEmpId.executeQuery();
                                PreparedStatement statement = connection.prepareStatement(
                                        "INSERT INTO attendance_salary_link " +
                                                "(id, employee_id, month_year, night_shift_exist, a_status, p_status, created_at, updated_at) " +
                                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                                )
                        ) {
                            int count = 0;

                            while (rs.next()) {
                                statement.setString(1, UUID.randomUUID().toString());
                                statement.setString(2, rs.getString("emp_id"));
                                statement.setString(3, monthYear);
                                statement.setInt(4, 0);
                                statement.setString(5, "PENDING");
                                statement.setString(6, "PENDING");
                                statement.setObject(7, LocalDateTime.now());
                                statement.setObject(8, LocalDateTime.now());
                                statement.addBatch();
                                count++;
                            }

                            if (count > 0) {
                                statement.executeBatch();
                                connection.commit();
                                System.out.println("[" + clinicId + "] Inserted " + count + " new attendance salary links for " + monthYear);
                            } else {
                                System.out.println("[" + clinicId + "] No new employees to link for " + monthYear);
                                connection.commit();
                            }
                        }

                    } catch (Exception e) {
                        connection.rollback();
                        throw e;
                    }

                } catch (java.sql.SQLSyntaxErrorException e) {
                    System.out.println("Skipping DB: " + clinicId + " — table not found: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}