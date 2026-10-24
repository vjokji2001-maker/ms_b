package com.smartcare.hrms.common.repository.attendance.application.leave;

import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequestDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveODRequestApproverDetailsRepository extends JpaRepository<LeaveODRequestDtls, Long> {

    @Query(value = "SELECT SQL_NO_CACHE lod.id AS approverDtlId, " + "lod.lv_rqst_id AS leaveRqstId, " + "lod.sequence AS sequence, " + "lod.aprvr_id AS approverId, " +
            "aed.formal_name AS approverName, " + "aed.emp_code AS approverCode, " + "lod.status AS status, " +
            "lod.rmrk AS approverRemark, " + "lod.action_date_time AS actionDateTime, " + "lor.id AS empLeaveRqstId, " +
            "lor.emp_id AS empId, " + "ed.formal_name AS name, " + "ed.emp_code AS code, " + "lor.app_dt AS applicationDate, " +
            "lor.frm_dt AS fromDate, " + "lor.frm_dt_typ AS fromDateDuration, " + "lor.to_dt AS toDate, " +
            "lor.to_dt_typ AS toDateDuration, " + "lor.reason AS reason, " + "lor.apprvr_rmrk AS remark " + "FROM lv_od_rqst_dtls lod " +
            "INNER JOIN lv_od_rqst lor ON lor.id = lod.lv_rqst_id " +
            "INNER JOIN emp_dtls ed ON ed.id = lor.emp_id " +
            "LEFT JOIN emp_dtls aed ON aed.id = lod.aprvr_id " +
            "WHERE lod.lv_rqst_id = (SELECT lv_rqst_id FROM lv_od_rqst_dtls WHERE id = :approverDtlId) " +
            "ORDER BY lod.sequence",
            nativeQuery = true)
    List<com.smartcare.hrms.common.bean.application.leave.LeaveRequestApproverDetailsBean> getByApproverDtlsId(@Param("approverDtlId") Long id);


}
