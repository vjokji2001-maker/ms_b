package com.smartcare.hrms.common.repository.payroll.statutory;

import com.smartcare.hrms.common.entity.payroll.statutory.EmployeeStatutoryInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeStatutoryInfoRepository extends JpaRepository<EmployeeStatutoryInfo, UUID> {
    @Query("SELECT esi\n" +
            "       FROM EmployeeStatutoryInfo esi\n" +
            "       JOIN FETCH esi.employee e\n" +
            "       JOIN FETCH e.branch b\n" +
            "       WHERE e.id = :employeeId")
    Optional<EmployeeStatutoryInfo> findByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT esi\n" +
            "       FROM EmployeeStatutoryInfo esi\n" +
            "       JOIN FETCH esi.employee e\n" +
            "       JOIN FETCH e.branch b\n" +
            "       WHERE e.id IN (:employeeId) ")
    List<EmployeeStatutoryInfo> findByEmployeeIdIn(@Param("employeeId") List<UUID> employeeId);

}
