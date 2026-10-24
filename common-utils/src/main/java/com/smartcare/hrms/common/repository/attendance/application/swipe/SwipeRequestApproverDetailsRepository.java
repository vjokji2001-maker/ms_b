package com.smartcare.hrms.common.repository.attendance.application.swipe;

import com.smartcare.hrms.common.bean.attendance.SwipeRequestApproverDetailsBean;
import com.smartcare.hrms.common.entity.attendance.application.swipe.SwipeRequestApproverDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwipeRequestApproverDetailsRepository extends JpaRepository<SwipeRequestApproverDetails, Long> {

   /* Approver Details */
  String empQuery = "SELECT SQL_NO_CACHE sra.id AS approverDtlId, sra.swp_rqst_id AS swpRqstId, sra.sequence AS sequence, sra.approver_id AS approverId, "
                  + "aed.formal_name AS approverName, aed.emp_code AS approverCode, sra.status AS swipeStatus, sra.remark AS approverRemark, sra.action_date_time AS actionDateTime, "
   /* Employee Swipe Request */
                  + "esr.id AS empSwpRqstId, esr.emp_id AS empId, ed.formal_name AS name, ed.emp_code AS code, esr.swp_date AS swpDate, DATE(esr.created_date_time) AS applicationDate, "
                  + "esr.day_type AS dayType, esr.direction AS swipeDirection, esr.emp_shift_id AS empShiftId, esr.req_in_time AS reqInTime, esr.req_out_time AS reqOutTime, "
                  + "esr.status AS empSwpStatus, esr.remark AS empSwpRemark, esr.created_date_time AS createdDateTime, esr.modified_date_time AS modifiedDateTime, "
                  + "esr.created_by AS createdBy, esr.modified_by AS modifiedBy, "
   /* Employee Shift  */
                  + "es.id AS empShiftPkId, es.emp_id AS empShiftEmpId, st.id AS shiftId, st.name AS shiftName, st.code AS shiftCode, st.in_time AS shiftInTime, "
                  + "st.out_time AS shiftOutTime, st.fh_out_time AS firstHalfOutTime, st.sh_in_time AS secondHalfInTime, es.date AS empShiftDate, es.is_default AS isDefault "
                  + "FROM swp_rqst_aprvr_dtl AS sra "
                  + "INNER JOIN emp_swp_rqst AS esr ON esr.id = sra.swp_rqst_id "
  /* Employee Details (Requester) */
                  + "INNER JOIN emp_dtls AS ed ON ed.id = esr.emp_id "
  /* Approver Employee Details */
                  + "LEFT JOIN emp_dtls AS aed ON aed.id = sra.approver_id "
                  + "LEFT JOIN emp_shift AS es ON es.id = esr.emp_shift_id "
                  + "LEFT JOIN shift_tmplt AS st ON st.id = es.shift_id "
                  + "WHERE sra.swp_rqst_id = (SELECT swp_rqst_id FROM swp_rqst_aprvr_dtl WHERE id = :approverDtlId) "
                  +  "ORDER BY sra.sequence";
    @Query(value = empQuery, nativeQuery = true)
    List<SwipeRequestApproverDetailsBean> getByApproverDtlId(@Param("approverDtlId") Long id);
}
