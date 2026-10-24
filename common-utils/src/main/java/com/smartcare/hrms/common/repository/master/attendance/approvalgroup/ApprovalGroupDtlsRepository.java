package com.smartcare.hrms.common.repository.master.attendance.approvalgroup;

import com.smartcare.hrms.common.entity.master.attendance.approvalgroup.ApprovalGroupDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalGroupDtlsRepository extends JpaRepository<ApprovalGroupDtls,Long> {

    Optional<ApprovalGroupDtls> findByApprovalGroup_IdAndEmployee_Id(UUID approvalGroupId, UUID employeeId);

    long countByApprovalGroup_Id(UUID approvalGroupId);

}
