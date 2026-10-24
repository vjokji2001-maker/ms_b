package com.smartcare.hrms.common.repository.payroll.compensation.employee;

import com.smartcare.hrms.common.entity.payroll.compensation.employee.EmpCompensationHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmpCompensationHdrRepository extends JpaRepository<EmpCompensationHdr, UUID> {

    static final String GET_BY_ID = """
                SELECT DISTINCT hdr
                FROM EmpCompensationHdr hdr
                JOIN FETCH hdr.compensationDetails dtls
                JOIN FETCH dtls.componentMaster
            """;

    @Query(GET_BY_ID+" WHERE hdr.id = :id")
    @Override
    Optional<EmpCompensationHdr> findById(@Param("id") UUID id);

    @Query(GET_BY_ID+" WHERE hdr.empId = :employeeId")
    Optional<EmpCompensationHdr> findByEmployeeId(@Param("employeeId") String employeeId);

    @Query(GET_BY_ID+" WHERE hdr.empId IN (:employeeId) ")
    List<EmpCompensationHdr> findByEmployeeIdIn(@Param("employeeId") List<String> employeeId);

     boolean existsByEmpId(String empId);

     boolean existsByEmpIdAndIdNot(String empId, UUID id);
}
