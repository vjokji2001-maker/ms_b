package com.smartcare.hrms.attendance.dao.application.leave;

import com.smartcare.hrms.attendance.dto.application.leave.*;
import com.smartcare.hrms.common.enumeration.ApplicationType;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.pagination.ColumnMeta;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.pagination.PaginationInfo;
import com.smartcare.hrms.common.pagination.PaginationUtils;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.ValidateUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CancelRequestApproverDashboardDAO {

    private @Autowired EntityManager entityManager;

    private @Autowired PaginationUtils paginationUtils;

    public static final String countQuery= "SELECT COUNT(*) FROM  lv_od_rqst lor "+
            "INNER JOIN emp_dtls AS ed ON ed.id = lor.emp_id  "+
            "INNER JOIN company_details c ON c.id = ed.company_id "+
            "INNER JOIN branch_details b ON b.id = ed.branch_id "+
            "INNER JOIN department dp ON dp.id = ed.department_id "+
            "INNER JOIN designation dg ON dg.id = ed.designation_id  "+
            "INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id "+
            "INNER JOIN category ct ON ct.id = ed.category_id "+
            "INNER JOIN grades g ON g.id = ed.grade_id  WHERE lor.app_type='LEAVE_CANCEL' " ;

    private static List<ColumnMeta> columns = new ArrayList<>(List.of(new ColumnMeta[]{
            new ColumnMeta("Emp Name", "ed.formal_name"),
            new ColumnMeta("Emp Code", "ed.emp_code"),
            new ColumnMeta("App Type", "lor.app_type"),
            new ColumnMeta("Leave Name", "ln.name"),
            new ColumnMeta("App Date", "lor.app_dt"),
            new ColumnMeta("From Date", "lor.frm_dt"),
            new ColumnMeta("Duration", "lor.frm_dt_typ"),
            new ColumnMeta("To Date", "lor.to_dt"),
            new ColumnMeta("Duration", "lor.to_dt_typ"),
            new ColumnMeta("Rqst Reason", "lor.reason"),
            new ColumnMeta("Status", "lor.status"),
            new ColumnMeta("Remark", "lord.rmrk"),
            new ColumnMeta("Last Remark", "lor.apprvr_rmrk AS lastApproverRemark"),
    }));

    public PaginatedDataResponse<CancelRequestApproverDashboardDTO> getData(CancelRequestApproverSearchDTO dto) {


        Integer count= ((Long) entityManager.createNativeQuery(countQuery).getSingleResult()).intValue();

        PaginationInfo paginationInfo=null;
        if(ValidateUtils.isNullObject(dto.getPaginationInfo())){
            paginationInfo=paginationUtils.getPaginationInfoObject();
        }else{
            paginationInfo=dto.getPaginationInfo();
        }
        paginationInfo.setTotalItems(count);

        StringBuffer sqlQuery = new StringBuffer();
        sqlQuery.append("SELECT lord.id AS lvRqstApproveId, ed.formal_name AS name, ed.emp_code AS code,lor.app_type AS applicationType,ln.name AS leaveName,DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate,  ");
        sqlQuery.append("lor.frm_dt_typ AS fromDateDuration,DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate,lor.to_dt_typ AS toDateDuration,lor.reason AS reason, lor.status AS status, lord.rmrk AS remark, ");
        sqlQuery.append("(SELECT lord1.rmrk FROM lv_od_rqst_dtls lord1 WHERE lord1.lv_rqst_id = lor.id AND lord1.rmrk IS NOT NULL ORDER BY lord1.sequence DESC LIMIT 1) AS lastApproverRemark,lor.id AS requestId ");
        sqlQuery.append("FROM lv_od_rqst lor ");
        sqlQuery.append("LEFT JOIN leave_name ln ON ln.id = lor.lv_nm_id ");
        sqlQuery.append("INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id= lor.id AND lord.aprvr_id='"+dto.getApproverEmpId()+"' AND lord.status IS NOT NULL ");
        sqlQuery.append("INNER JOIN emp_dtls AS ed ON ed.id = lor.emp_id  ");
        sqlQuery.append("INNER JOIN company_details c ON c.id = ed.company_id ");
        sqlQuery.append(" INNER JOIN branch_details b ON b.id = ed.branch_id ");
        sqlQuery.append("INNER JOIN department dp ON dp.id = ed.department_id ");
        sqlQuery.append("INNER JOIN designation dg ON dg.id = ed.designation_id  ");
        sqlQuery.append("INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id ");
        sqlQuery.append("INNER JOIN category ct ON ct.id = ed.category_id ");
        sqlQuery.append("INNER JOIN grades g ON g.id = ed.grade_id ");
        sqlQuery.append(" WHERE lor.app_type='LEAVE_CANCEL' ");

        if(!ValidateUtils.isStringEmpty(dto.getName())) {
            sqlQuery.append(" AND ed.formal_name LIKE '%").append(dto.getName().trim().replace("'", "''")).append("%'");
        }
        if(!ValidateUtils.isStringEmpty(dto.getCode())) {
            sqlQuery.append(" AND ed.emp_code = '").append(dto.getCode().trim().replace("'", "''")).append("' ");
        }
        if(!ValidateUtils.isLongZero(dto.getCompanyId())) {
            sqlQuery.append("AND cd.id =   ").append(dto.getCompanyId()).append("");
        }
        if(!ValidateUtils.isLongZero(dto.getBranchId())) {
            sqlQuery.append("AND bd.id =   ").append(dto.getBranchId()).append("");
        }
        if(!ValidateUtils.isLongZero(dto.getDepartmentId())) {
            sqlQuery.append("AND d.id =   ").append(dto.getDepartmentId()).append("");
        }
        if(!ValidateUtils.isLongZero(dto.getDesignationId())) {
            sqlQuery.append("AND dg.id=  ").append(dto.getDesignationId()).append("");
        }
        if (!ValidateUtils.isNullObject(dto.getWorkLocationId())) {
            sqlQuery.append(" AND ed.work_location_id = ").append(dto.getWorkLocationId()).append("");
        }
        if (!ValidateUtils.isNullObject(dto.getCategoryId())) {
            sqlQuery.append(" AND ed.category_id = ").append(dto.getCategoryId()).append("");
        }
        if (!ValidateUtils.isNullObject(dto.getGradeId())) {
            sqlQuery.append(" AND ed.grade_id = ").append(dto.getGradeId()).append("");
        }
        if (!ValidateUtils.isStringEmpty(dto.getFromDate())) {
            LocalDate fromDate = DateHelper.convertUserDateToLocalDate(dto.getFromDate());
            sqlQuery.append("AND lor.app_dt >= '").append(fromDate).append("' ");
        }
        if (!ValidateUtils.isStringEmpty(dto.getToDate())) {
            LocalDate toDate = DateHelper.convertUserDateToLocalDate(dto.getToDate());
            sqlQuery.append("AND lor.app_dt <= '").append(toDate).append("' ");
        }
        if (!ValidateUtils.isStringEmpty(dto.getStatus())) {
            sqlQuery.append(" AND lor.status = :status ");
        }
        sqlQuery.append(paginationUtils.getSortingQuery(columns, paginationInfo));
        sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));

        Query nativeQuery = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);

        List<Tuple> results = nativeQuery.getResultList();
        List<Object> dataList= new ArrayList<>();
        if(!CollectionUtils.isEmpty(results)) {
            for(Tuple result:results) {
                CancelRequestApproverDashboardDTO dashboardDTO= CancelRequestApproverDashboardDTO.builder()
                        .approverId(result.get("lvRqstApproveId", Long.class))
                        .name(result.get("name", String.class))
                        .code(result.get("code", String.class))
                        .applicationType(EnumConverter.toDisplayString(result.get("applicationType", String.class), ApplicationType.class))
                        .leaveName(result.get("leaveName", String.class))
                        .applicationDate(result.get("applicationDate", String.class))
                        .fromDate(result.get("fromDate",String.class))
                        .fromDateDuration(EnumConverter.toDisplayString(result.get("fromDateDuration", String.class), DayType.class))
                        .toDate(result.get("toDate", String.class))
                        .toDateDuration(EnumConverter.toDisplayString(result.get("toDateDuration", String.class), DayType.class))
                        .reason(result.get("reason", String.class))
                        .status(EnumConverter.toDisplayString(result.get("status", String.class), ApplicationStatus.class))
                        .remark(result.get("remark", String.class))
                        .lastApproverRemark(result.get("lastApproverRemark", String.class))
                        .requestId(result.get("requestId", String.class))
                        .build();
                dataList.add(dashboardDTO);


            }

        }
        return (PaginatedDataResponse<CancelRequestApproverDashboardDTO>) paginationUtils.getPaginatedResponse(columns,dataList,dto.getPaginationInfo());
    }

}
