package com.smartcare.hrms.common.repository.attendance.application.leave;


import com.smartcare.hrms.common.bean.application.leave.ApprovalBean;
import com.smartcare.hrms.common.bean.application.leave.LeaveReportBean;
import com.smartcare.hrms.common.bean.application.leave.LeaveRequestBean;
import com.smartcare.hrms.common.bean.attendance.EmployeeLeaveDataBean;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveODRequest, UUID> {


    @Query(value = "SELECT frm_dt AS fromDate,to_dt AS toDate,ttl_cnt AS totalLeaveCount," +
            "frm_dt_typ AS fromDateType ,to_dt_typ AS toDateType,status,lv_nm_id AS leaveNameId " +
            "FROM lv_od_rqst " +
            "WHERE app_type='LEAVE_RQST' AND emp_id=:empId AND yr_id=:yearId ORDER BY frm_dt ASC ",nativeQuery = true)
    List<LeaveRequestBean> getEmployeeLeaveRequestDataByEmpIdAndYearId(@Param("empId") String empId, @Param("yearId") Long yearId) ;

    @Query(value = "SELECT eld.yrly_count AS yearlyCount, eld.qtrly_count AS quarterlyCount," +
            "eld.mnthly_count AS monthlyCount,eld.pst_days AS pastDaysAllowed,eld.future_days AS futureDaysAllowed, " +
            "eld.half_day_allowed AS halfDayAllowed,ln.id AS leaveNameId, ln.name AS leaveName " +
            "FROM emp_leave_prnt elp " +
            "INNER JOIN emp_leave_dtls eld ON  elp.id = eld.enp_leave_id  " +
            "INNER JOIN leave_name ln ON ln.id= eld.leave_name_id "+
            "WHERE elp.emp_id=:empId AND elp.year_id=:yearId ",nativeQuery = true)
    List<EmployeeLeaveDataBean> getLeaveDetailsByYearAndEmpId(@Param("empId") String empId, @Param("yearId") Long yearId);


    @Query(value="SELECT lor.id AS leaveRequestId,ln.name AS leaveName,lor.lv_nm_id AS leaveNameId,  lor.frm_dt AS fromDate, lor.frm_dt_typ AS fromDateType,lor.app_type AS applicationType, " +
            "lor.to_dt AS toDate,lor.to_dt_typ AS toDateType,lor.emp_id AS empId, lor.yr_id AS yearId,lor.ttl_cnt AS totalLeaveCount,lor.status AS status,lor.app_dt AS applicationDate, " +
            "lor.reason AS reason,lord.aprvr_id AS approverId,ed.formal_name AS approverName, ed.emp_code AS approverCode, " +
            "lord.sequence AS sequence,lord.status AS approverStatus,lord.rmrk AS remark, lord.action_date_time AS actionDateTime " +
            "FROM lv_od_rqst lor  " +
            "INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id= lor.id " +
            "INNER JOIN emp_dtls AS ed ON ed.id = lord.aprvr_id " +
            "LEFT JOIN leave_name ln ON ln.id= lor.lv_nm_id " +
            "WHERE lor.id=:requestId  AND lor.app_type='LEAVE_RQST' " +
            "order by lord.sequence",nativeQuery = true)
    List<LeaveRequestBean> getLeaveRequestById(@Param("requestId") String requestId);

    @Query(value="SELECT lor.id AS leaveRequestId,ln.name AS leaveName,lor.lv_nm_id AS leaveNameId,  lor.frm_dt AS fromDate, lor.frm_dt_typ AS fromDateType,lor.app_type AS applicationType, " +
            "lor.to_dt AS toDate,lor.to_dt_typ AS toDateType,lor.emp_id AS empId, lor.yr_id AS yearId,lor.ttl_cnt AS totalLeaveCount,lor.status AS status, lor.app_dt AS applicationDate," +
            "lor.reason AS reason,lord.aprvr_id AS approverId,ed.formal_name AS approverName, ed.emp_code AS approverCode, " +
            "lord.sequence AS sequence,lord.status AS approverStatus,lord.rmrk AS remark, lord.action_date_time AS actionDateTime " +
            "FROM lv_od_rqst lor  " +
            "INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id= lor.id " +
            "INNER JOIN emp_dtls AS ed ON ed.id = lord.aprvr_id " +
            "LEFT JOIN leave_name ln ON ln.id= lor.lv_nm_id " +
            "WHERE lor.id=:requestId  AND lor.app_type='LEAVE_CANCEL' " +
            "order by lord.sequence",nativeQuery = true)
    List<LeaveRequestBean> getCancelLeaveRequestById(@Param("requestId") String requestId);


    @Query(value = "SELECT lor.app_type AS applicationType, lor.frm_dt AS fromDate, lor.to_dt AS toDate ," +
            " lor.frm_dt_typ AS fromDateType, lor.to_dt_typ AS toDateType , ln.code AS leaveCode " +
            "FROM lv_od_rqst lor LEFT JOIN leave_name ln ON ln.id = lor.lv_nm_id " +
            "WHERE lor.status = 'APPROVED' " +
            "AND lor.app_type NOT IN ('OD_CANCEL','LEAVE_CANCEL') " +
            "AND lor.emp_id = :empId " +
            "AND lor.frm_dt <= :toDate AND lor.to_dt >= :fromDate ",nativeQuery = true)
    List<LeaveRequestBean> getLeaveDetailsForMonth(
            @Param("empId") String empId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);


    @Query(value = "SELECT * FROM vw_employee_leave_report "
            +" WHERE (:fromDate IS NULL OR fromDate >= :fromDate) "
            + "AND (:toDate IS NULL OR toDate <= :toDate) "
            + "AND (:name IS NULL OR employeeName LIKE CONCAT('%',:name,'%')) "
            + "AND (:code IS NULL OR employeeCode=:code ) "
            + "AND (:departmentId IS NULL OR departmentId=:departmentId) "
            + "AND (:companyId IS NULL OR companyId=:companyId) "
            + "AND (:branchId IS NULL OR branchId=:branchId) "
            + "AND (:designationId IS NULL OR designationId=:designationId) "
            + "AND (:categoryId IS NULL OR categoryId=:categoryId) "
            + "AND (:workLocationId IS NULL OR workLocationId=:workLocationId) "
            + "AND (:gradeId IS NULL OR gradeId=:gradeId) "
            , nativeQuery = true)
    List<LeaveReportBean> getLeaveReport(@Param("fromDate") String fromDate, @Param("toDate") String toDate,
                                         @Param("name") String name, @Param("code") String code,
                                         @Param("departmentId") Integer departmentId, @Param("companyId") Integer companyId,
                                         @Param("branchId") Integer branchId, @Param("designationId") Integer designationId,
                                         @Param("categoryId") Integer categoryId, @Param("workLocationId") Integer workLocationId,
                                         @Param("gradeId") Integer gradeId
    );

    @Query(value = " SELECT "+
            "apr.lv_rqst_id AS requestId, "+
            "apr.sequence AS level, "+
            "apr.status, "+
            "apr.rmrk AS remark, "+
            "apr.action_date_time AS actionDateTime, "+
            "ed.formal_name AS approverName "+
            "FROM lv_od_rqst_dtls apr "+
            "LEFT JOIN emp_dtls ed ON ed.id = apr.aprvr_id " +
            " WHERE apr.lv_rqst_id IN (:requestIds) "+
            " ORDER BY apr.lv_rqst_id, apr.sequence "
            , nativeQuery = true)
    List<ApprovalBean> getApprovalList(List<String> requestIds);
}
