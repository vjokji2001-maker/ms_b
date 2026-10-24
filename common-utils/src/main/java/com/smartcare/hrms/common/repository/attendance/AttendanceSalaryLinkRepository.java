package com.smartcare.hrms.common.repository.attendance;

import com.smartcare.hrms.common.entity.attendance.AttendanceSalaryLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceSalaryLinkRepository extends JpaRepository<AttendanceSalaryLink, UUID> {


    boolean existsByEmployeeIdAndMonthYear(String employeeId, String monthYear);

    List<AttendanceSalaryLink> findByMonthYear(String monthYear);

    @Modifying
    @Query(value = "UPDATE attendance_salary_link  SET a_status = 'RECALCULATE' " +
            "WHERE employee_id IN (:empIds) AND month_year = :monthYear " +
            "AND a_status NOT IN ('LOCKED', 'FROZEN')", nativeQuery = true)
    int markAsRecalculate(@Param("empIds") List<String> empIds,
                          @Param("monthYear") String monthYear);
}
