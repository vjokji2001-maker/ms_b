package com.smartcare.hrms.common.repository.attendance.application.leave;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestApproverDetailsBean;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequestDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestDtlsRepository extends JpaRepository<LeaveODRequestDtls, Long> {

    @Query(value="SELECT SQL_NO_CACHE lord.id AS approverDtlId,lord.lv_rqst_id AS leaveRqstId,lord.sequence AS sequence, lord.aprvr_id AS approverId, " +
            "aed.formal_name AS approverName, aed.emp_code AS approverCode,lord.status AS status, lord.rmrk AS approverRemark, lord.action_date_time AS actionDateTime, " +
            "lor.id AS empLeaveRqstId, lor.emp_id AS empId, ed.formal_name AS name, ed.emp_code AS code,lor.app_dt AS applicationDate,lor.app_type AS applicationType,   " +
            "lor.frm_dt AS fromDate,lor.frm_dt_typ AS fromDateDuration,lor.to_dt AS toDate,lor.to_dt_typ AS toDateDuration, " +
            "lor.reason AS reason, lor.apprvr_rmrk AS remark ," +
            "( SELECT ln.name FROM leave_name ln WHERE ln.id = lor.lv_nm_id  LIMIT 1) AS leaveName "+
            " FROM lv_od_rqst_dtls lord " +
            "INNER JOIN lv_od_rqst lor ON lor.id= lord.lv_rqst_id " +
            "INNER JOIN emp_dtls AS ed ON ed.id = lor.emp_id " +
            "LEFT JOIN emp_dtls AS aed ON aed.id = lord.aprvr_id " +
            "WHERE lord.lv_rqst_id = (SELECT lv_rqst_id FROM lv_od_rqst_dtls WHERE id =:id) " +
            "ORDER BY lord.sequence ", nativeQuery = true)
    List<LeaveRequestApproverDetailsBean> getByApproverDtlsId(@Param("id") Long id);
}
