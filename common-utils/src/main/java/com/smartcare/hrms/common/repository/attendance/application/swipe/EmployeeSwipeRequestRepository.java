package com.smartcare.hrms.common.repository.attendance.application.swipe;

import com.smartcare.hrms.common.bean.attendance.EmployeeShiftDetailBean;
import com.smartcare.hrms.common.bean.attendance.EmployeeSwipeRequestBean;
import com.smartcare.hrms.common.entity.attendance.application.swipe.EmployeeSwipeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeSwipeRequestRepository extends JpaRepository<EmployeeSwipeRequest, UUID> {

   /*Employee Swipe Request*/
   String empQuery = "SELECT SQL_NO_CACHE esr.id AS id, esr.emp_id AS requestEmpId, esr.swp_date AS swipeDate, DATE(esr.created_date_time) AS applicationDate, "
                   + "esr.day_type AS dayType, esr.direction AS swipeDirection, esr.req_in_time AS reqInTime, esr.req_out_time AS reqOutTime, "
                   + "esr.status AS swipeStatus, esr.remark AS remark, esr.created_date_time AS createdDateTime, esr.modified_date_time AS modifiedDateTime, "
                   + "esr.created_by AS createdBy, esr.modified_by AS modifiedBy, "
    /*Employee Shift */
                   + "es.id AS empShiftId, es.emp_id AS shiftEmpId, es.shift_id AS shiftId, st.name AS shiftName, st.code AS shiftCode, st.in_time AS shiftInTime, "
                   + "st.out_time AS shiftOutTime, st.fh_out_time  AS firstHalfOutTime, st.sh_in_time AS secondHalfInTime, es.date AS empShiftDate, es.is_default AS isDefault, "
    /*Swipe Approver */
                   + "sra.id AS swipeRequestApproverId, sra.sequence AS sequence, sra.approver_id AS approverId, ed.formal_name AS approverName, ed.emp_code AS approverCode, "
                   + "sra.action_date_time AS actionDateTime, sra.status AS approverSwipeStatus, sra.remark AS approverRemark "
                   + "FROM emp_swp_rqst AS esr "
                   + "LEFT JOIN emp_shift AS es ON es.id = esr.emp_shift_id "
                   + "LEFT JOIN shift_tmplt AS st ON st.id = es.shift_id "
                   + "LEFT JOIN swp_rqst_aprvr_dtl AS sra ON sra.swp_rqst_id = esr.id "
                   + "LEFT JOIN emp_dtls AS ed ON ed.id = sra.approver_id "
                   + "WHERE esr.id = :id "
                   + "ORDER BY sra.sequence ";
    @Query(value = empQuery, nativeQuery = true)
    List<EmployeeSwipeRequestBean> getAllId(@Param("id") String id);


    String shiftQuery = "SELECT es.id AS empShiftId, st.id AS shiftTemplateId, st.name AS shiftName, st.code AS shiftCode, st.in_time AS shiftInTime, "
                      + "st.out_time AS shiftOutTime, st.fh_out_time AS firstHalfOutTime, st.sh_in_time AS secondHalfInTime, es.date AS empShiftDate "
                      + "FROM shift_tmplt AS st "
                      + "INNER JOIN emp_shift AS es ON es.shift_id = st.id "
                      + "WHERE es.date = :date AND es.emp_id = :empId ";
    @Query(value = shiftQuery, nativeQuery = true)
    EmployeeShiftDetailBean getEmpShiftByDate(@Param("date") LocalDate date, @Param("empId") String empId);
}
