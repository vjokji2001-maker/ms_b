package com.smartcare.hrms.common.repository.attendance.application.swipe;

import com.smartcare.hrms.common.bean.attendance.EmployeeDailyPunchLogBean;
import com.smartcare.hrms.common.entity.attendance.application.swipe.EmployeeDailyPunchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmployeeDailyPunchLogRepository extends JpaRepository<EmployeeDailyPunchLog, Long> {

    List<EmployeeDailyPunchLog> findByEmpCodeAndPunchDate(String empCode, LocalDate date);

    @Query(value="SELECT edpl.id AS id,ed.id AS empId, punch_date AS punchDate, punch_date_time AS punchDateTime, direction AS direction FROM emp_daily_punch_log edpl " +
            "INNER JOIN emp_dtls ed ON ed.emp_code=edpl.emp_code " +
            "WHERE  ed.id =:empId AND  punch_date_time BETWEEN :fromDate AND :toDate ",nativeQuery = true)
    List<EmployeeDailyPunchLogBean> getPunchLogData(@Param("empId") String empId ,@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
}
