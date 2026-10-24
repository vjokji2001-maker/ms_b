package com.smartcare.hrms.common.repository.attendance.application.leave;

import com.smartcare.hrms.common.bean.application.leave.LeaveODRequestBean;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveODRequestRepository extends JpaRepository<LeaveODRequest, UUID> {

    @Query(value = "SELECT lor.frm_dt AS fromDate, lor.to_dt AS toDate, lor.ttl_cnt AS totalLeaveCount, " +
            "lor.frm_dt_typ AS fromDateType, lor.to_dt_typ AS toDateType, lor.status AS status " +
            "FROM lv_od_rqst lor " +
            "WHERE lor.app_type='OD_RQST' AND lor.emp_id=:empId ORDER BY lor.frm_dt ASC ", nativeQuery = true)
    List<LeaveODRequestBean> getEmployeeLeaveRequestDataByEmpId(@Param("empId") String empId);


    @Query(value="SELECT lor.id AS requestId, lor.emp_id AS requestEmpId, lor.frm_dt AS fromDate, lor.frm_dt_typ AS fromDateType, " +
            "lor.to_dt AS toDate, lor.to_dt_typ AS toDateType, lor.app_dt AS applicationDate, lor.ttl_cnt AS totalCount, " +
            "lor.reason AS reason, lor.status AS status, lod.aprvr_id AS approverId, ed.formal_name AS approverName, ed.emp_code AS approverCode, " +
            "lod.sequence AS sequence, lod.status AS approverStatus, lod.rmrk AS remark, lod.action_date_time AS actionDateTime " +
            "FROM lv_od_rqst lor " +
            "LEFT JOIN lv_od_rqst_dtls lod ON lod.lv_rqst_id = lor.id " +
            "LEFT JOIN emp_dtls ed ON ed.id = lod.aprvr_id " +
            "WHERE lor.id=:requestId AND lor" +
            ".app_type='OD_RQST' " +
            "AND lod.status IS NOT NULL " +
            "ORDER BY lod.sequence", nativeQuery = true)
    List<LeaveODRequestBean> getODRequestById(@Param("requestId") String requestId);


    @Query(value = "SELECT lor.id AS leaveRequestId, " +
            "lor.frm_dt AS fromDate, lor.frm_dt_typ AS fromDateType, " +
            "lor.to_dt AS toDate, lor.to_dt_typ AS toDateType, " +
            "lor.emp_id AS empId, lor.yr_id AS yearId, lor.ttl_cnt AS totalLeaveCount, lor.status AS status, " +
            "lor.reason AS reason, lord.aprvr_id AS approverId, ed.formal_name AS approverName, ed.emp_code AS approverCode, " +
            "lord.sequence AS sequence, lord.status AS approverStatus, lord.rmrk AS remark, lord.action_date_time AS actionDateTime " +
            "FROM lv_od_rqst lor " +
            "INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id = lor.id " +
            "INNER JOIN emp_dtls AS ed ON ed.id = lord.aprvr_id " +
            "WHERE lor.id=:requestId AND lor.app_type='OD_CANCEL' " +
            "ORDER BY lord.sequence", nativeQuery = true)
    List<LeaveODRequestBean> getCancelODRequestById(@Param("requestId") String requestId);


    @Modifying
    @Transactional
    @Query(value="UPDATE lv_od_rqst  SET status='CANCELED' WHERE id=:requestId AND app_type='LEAVE_RQST' AND status='APPROVED' ", nativeQuery = true)
    int updateLeaveStatus(@Param("requestId") String requestId);

    @Query(value = "SELECT leave_rqst_id FROM lv_od_rqst WHERE id=:requestId ", nativeQuery = true)
    String getLeaveRequestId(@Param("requestId") String requestId);


    @Modifying
    @Transactional
    @Query(value = "UPDATE lv_od_rqst SET status='CANCELED' WHERE id=:requestId AND app_type='OD_RQST' AND status='APPROVED'", nativeQuery = true)
    int updateODStatus(@Param("requestId") String requestId);

    @Query(value = "SELECT leave_rqst_id FROM lv_od_rqst WHERE id=:requestId", nativeQuery = true)
    String getODRequestId(@Param("requestId") String requestId);
}



