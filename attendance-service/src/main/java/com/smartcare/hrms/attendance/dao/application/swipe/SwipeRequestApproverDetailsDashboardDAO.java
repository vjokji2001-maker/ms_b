package com.smartcare.hrms.attendance.dao.application.swipe;

import com.smartcare.hrms.attendance.dto.application.swipe.SwipeRequestApproverDetailsDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.SwipeRequestApproverSearchDTO;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.enumeration.SwipeDirection;
import com.smartcare.hrms.common.enumeration.SwipeStatus;
import com.smartcare.hrms.common.pagination.ColumnMeta;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.pagination.PaginationInfo;
import com.smartcare.hrms.common.pagination.PaginationUtils;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.TimeHelper;
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

import static com.smartcare.hrms.common.utility.TimeHelper.convertSqlTimeToLocalTime;

@Repository
public class SwipeRequestApproverDetailsDashboardDAO {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PaginationUtils paginationUtils;


    private final static String baseCountQuery = "SELECT COUNT(*) FROM emp_swp_rqst AS esr "
            + "LEFT JOIN emp_shift AS es ON es.id = esr.emp_shift_id "
            + "LEFT JOIN shift_tmplt AS st ON st.id = es.shift_id "
            + "INNER JOIN emp_dtls ed ON ed.id = esr.emp_id "
            + "INNER JOIN company_details c ON c.id = ed.company_id "
            + "INNER JOIN branch_details b ON b.id = ed.branch_id "
            + "INNER JOIN department dp ON dp.id = ed.department_id "
            + "INNER JOIN designation dg ON dg.id = ed.designation_id "
            + "INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id "
            + "INNER JOIN category ct ON ct.id = ed.category_id "
            + "INNER JOIN grades g ON g.id = ed.grade_id ";

    private static List<ColumnMeta> columns = new ArrayList<>(List.of(new ColumnMeta[]{
            new ColumnMeta("Emp Name", "ed.formal_name"),
            new ColumnMeta("Emp Code", "ed.emp_code"),
            new ColumnMeta("Swipe Date", "esr.swp_date"),
            new ColumnMeta("App Date", "esr.created_date_time"),
            new ColumnMeta("Day Type", "esr.day_type"),
            new ColumnMeta("Swipe Direction", "esr.direction"),
            new ColumnMeta("Shift Code", "st.code"),
            new ColumnMeta("Rqst In Time", "esr.req_in_time"),
            new ColumnMeta("Rqst Out Time", "esr.req_out_time"),
            new ColumnMeta("Status", "sra.status"),
            new ColumnMeta("Remark", "sra.remark AS remark"),
            new ColumnMeta("Last  Remark", "lastApproverRemark"),
    }));


    public PaginatedDataResponse<SwipeRequestApproverDetailsDashboardDTO> getData(SwipeRequestApproverSearchDTO searchDTO){

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");

        if (!ValidateUtils.isStringEmpty(searchDTO.getName())) {
            whereClause.append(" AND LOWER(ed.formal_name) LIKE LOWER(CONCAT('%', :name, '%')) ");
        }
        if (!ValidateUtils.isStringEmpty(searchDTO.getCode())) {
            whereClause.append(" AND LOWER(ed.emp_code) = LOWER(:code) ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getCompanyId())) {
            whereClause.append(" AND ed.company_id = :companyId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getBranchId())) {
            whereClause.append(" AND ed.branch_id = :branchId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getDepartmentId())) {
            whereClause.append(" AND ed.department_id = :departmentId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getDesignationId())) {
            whereClause.append(" AND ed.designation_id = :designationId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getWorkLocationId())) {
            whereClause.append(" AND ed.work_location_id = :workLocationId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getCategoryId())) {
            whereClause.append(" AND ed.category_id = :categoryId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getGradeId())) {
            whereClause.append(" AND ed.grade_id = :gradeId ");
        }
        if (!ValidateUtils.isStringEmpty(searchDTO.getFromDate())) {
            whereClause.append(" AND esr.swp_date >= :fromDate ");
        }
        if (!ValidateUtils.isStringEmpty(searchDTO.getToDate())) {
            whereClause.append(" AND esr.swp_date <= :toDate ");
        }
        if (!ValidateUtils.isStringEmpty(searchDTO.getStatus())) {
            whereClause.append(" AND sra.status = :status ");
        }


        String countSql = baseCountQuery + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        setQueryParameters(countQuery, searchDTO);
        int count = ((Number) countQuery.getSingleResult()).intValue();

        PaginationInfo paginationInfo = null;
        if (ValidateUtils.isNullObject(searchDTO.getPaginationInfo())){
            paginationInfo = paginationUtils.getPaginationInfoObject();
        } else {
            paginationInfo = searchDTO.getPaginationInfo();
        }
        paginationInfo.setTotalItems(count);

        // DATA QUERY
        StringBuffer sqlQuery = new StringBuffer();
        sqlQuery.append("SELECT sra.id AS requestId, sra.id AS swpReqApproverId, ed.formal_name AS name, ed.emp_code AS code, DATE_FORMAT(esr.swp_date, '%d/%m/%Y') AS swipeDate, DATE_FORMAT(esr.created_date_time, '%d/%m/%Y') AS applicationDate, ");
        sqlQuery.append("esr.day_type AS dayType, esr.direction AS swipeDirection, st.code AS shiftCode, esr.req_in_time AS reqInTime, esr.req_out_time AS reqOutTime, ");
        sqlQuery.append("esr.status AS status, esr.remark AS remark, ");
        sqlQuery.append("(SELECT sra2.remark FROM swp_rqst_aprvr_dtl sra2 WHERE sra2.swp_rqst_id = esr.id AND sra2.remark IS NOT NULL ORDER BY sra2.sequence DESC LIMIT 1) AS lastApproverRemark, esr.id AS empSwipeReqId ");
        sqlQuery.append("FROM emp_swp_rqst AS esr ");
        sqlQuery.append("INNER JOIN swp_rqst_aprvr_dtl AS sra ON sra.swp_rqst_id = esr.id AND sra.approver_id = '"+searchDTO.getApproverEmpId()+"' AND sra.status IS NOT NULL ");
        sqlQuery.append("INNER JOIN emp_shift AS es ON es.id = esr.emp_shift_id ");
        sqlQuery.append("INNER JOIN shift_tmplt AS st ON st.id = es.shift_id ");
        sqlQuery.append("INNER JOIN emp_dtls AS ed ON ed.id = esr.emp_id ");
        sqlQuery.append("INNER JOIN company_details c ON c.id = ed.company_id ");
        sqlQuery.append("INNER JOIN branch_details b ON b.id = ed.branch_id ");
        sqlQuery.append("INNER JOIN department dp ON dp.id = ed.department_id ");
        sqlQuery.append("INNER JOIN designation dg ON dg.id = ed.designation_id ");
        sqlQuery.append("INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id ");
        sqlQuery.append("INNER JOIN category ct ON ct.id = ed.category_id ");
        sqlQuery.append("INNER JOIN grades g ON g.id = ed.grade_id ");
        sqlQuery.append(whereClause);
        sqlQuery.append(paginationUtils.getSortingQuery(columns, paginationInfo));
        sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));

        // EXECUTE DATA QUERY
        Query dataQuery = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);
        setQueryParameters(dataQuery, searchDTO);

        List<Tuple> results = dataQuery.getResultList();

        List<Object> dataList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(results)){
            for (Tuple result : results) {
                TimeHelper.convertToString(convertSqlTimeToLocalTime(result.get("reqInTime")));
                SwipeRequestApproverDetailsDashboardDTO dashboardDTO = SwipeRequestApproverDetailsDashboardDTO.builder()
                        .swpReqApproverId(result.get("swpReqApproverId", Long.class))
                        .name(result.get("name", String.class))
                        .code(result.get("code", String.class))
                        .swipeDate(result.get("swipeDate", String.class))
                        .applicationDate(result.get("applicationDate", String.class))
                        .dayType(EnumConverter.toDisplayString(result.get("dayType", String.class), DayType.class))
                        .swipeDirection(EnumConverter.toDisplayString(result.get("swipeDirection", String.class), SwipeDirection.class))
                        .shiftCode(result.get("shiftCode", String.class))
                        .reqInTime(TimeHelper.convertToString(convertSqlTimeToLocalTime(result.get("reqInTime"))))
                        .reqOutTime(TimeHelper.convertToString(convertSqlTimeToLocalTime(result.get("reqOutTime"))))
                        .status(EnumConverter.toDisplayString(result.get("status", String.class), SwipeStatus.class))
                        .remark(result.get("remark", String.class))
                        .lastApproverRemark(result.get("lastApproverRemark", String.class))
                        .empSwipeReqId(result.get("empSwipeReqId", String.class))
                        .build();
                dataList.add(dashboardDTO);
            }
        }
        return (PaginatedDataResponse<SwipeRequestApproverDetailsDashboardDTO>) paginationUtils.getPaginatedResponse(columns, dataList, searchDTO.getPaginationInfo());
    }

    // Helper method to set parameters for both queries
    private void setQueryParameters(Query query, SwipeRequestApproverSearchDTO dto) {
        if (!ValidateUtils.isStringEmpty(dto.getName())) {
            query.setParameter("name", dto.getName().trim());
        }
        if (!ValidateUtils.isStringEmpty(dto.getCode())) {
            query.setParameter("code", dto.getCode().trim());
        }
        if (!ValidateUtils.isNullObject(dto.getCompanyId())) {
            query.setParameter("companyId", dto.getCompanyId());
        }
        if (!ValidateUtils.isNullObject(dto.getBranchId())) {
            query.setParameter("branchId", dto.getBranchId());
        }
        if (!ValidateUtils.isNullObject(dto.getDepartmentId())) {
            query.setParameter("departmentId", dto.getDepartmentId());
        }
        if (!ValidateUtils.isNullObject(dto.getDesignationId())) {
            query.setParameter("designationId", dto.getDesignationId());
        }
        if (!ValidateUtils.isNullObject(dto.getWorkLocationId())) {
            query.setParameter("workLocationId", dto.getWorkLocationId());
        }
        if (!ValidateUtils.isNullObject(dto.getCategoryId())) {
            query.setParameter("categoryId", dto.getCategoryId());
        }
        if (!ValidateUtils.isNullObject(dto.getGradeId())) {
            query.setParameter("gradeId", dto.getGradeId());
        }
        if (!ValidateUtils.isStringEmpty(dto.getFromDate())) {
            LocalDate fromDate = DateHelper.convertUserDateToLocalDate(dto.getFromDate());
            query.setParameter("fromDate", java.sql.Date.valueOf(fromDate));
        }
        if (!ValidateUtils.isStringEmpty(dto.getToDate())) {
            LocalDate toDate = DateHelper.convertUserDateToLocalDate(dto.getToDate());
            query.setParameter("toDate", java.sql.Date.valueOf(toDate));
        }
        if (!ValidateUtils.isStringEmpty(dto.getStatus())) {
            query.setParameter("status", dto.getStatus().trim());
        }
    }
}
