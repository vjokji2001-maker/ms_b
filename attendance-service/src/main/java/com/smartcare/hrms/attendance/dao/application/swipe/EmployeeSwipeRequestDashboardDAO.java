package com.smartcare.hrms.attendance.dao.application.swipe;

import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeSwipeRequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeSwipeRequestSearchDTO;
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
public class EmployeeSwipeRequestDashboardDAO {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PaginationUtils paginationUtils;


    private final static String baseCountQuery = "SELECT COUNT(*) FROM emp_swp_rqst AS esr ";


    private static List<ColumnMeta> columns = new ArrayList<>(List.of(new ColumnMeta[]{
            new ColumnMeta("Swipe Date", "esr.swp_date"),
            new ColumnMeta("App Date", "esr.created_date_time"),
            new ColumnMeta("Day Type", "esr.day_type"),
            new ColumnMeta("Swipe Direction", "esr.direction"),
            new ColumnMeta("Rqst In Time", "esr.req_in_time"),
            new ColumnMeta("Rqst Out Time", "esr.req_out_time"),
            new ColumnMeta("Status", "esr.status"),
            new ColumnMeta("Remark", "esr.remark"),
            new ColumnMeta("Last  Remark", "lastApproverRemark"),
    }));


    public PaginatedDataResponse<EmployeeSwipeRequestDashboardDTO> getData(EmployeeSwipeRequestSearchDTO searchDTO) {


        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");

        if (!ValidateUtils.isStringEmpty(searchDTO.getReqEmpId())) {
            whereClause.append(" AND esr.emp_id = :reqEmpId ");
        }
        if (!ValidateUtils.isStringEmpty(searchDTO.getFromDate())) {
            whereClause.append(" AND esr.swp_date >= :fromDate ");
        }
        if (!ValidateUtils.isStringEmpty(searchDTO.getToDate())) {
            whereClause.append(" AND esr.swp_date <= :toDate ");
        }
        if (!ValidateUtils.isStringEmpty(searchDTO.getStatus())) {
            whereClause.append(" AND esr.status = :status ");
        }

        String countSql = baseCountQuery + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        setQueryParameters(countQuery, searchDTO);
        int count = ((Number) countQuery.getSingleResult()).intValue();

        PaginationInfo paginationInfo = null;
        if (ValidateUtils.isNullObject(searchDTO.getPaginationInfo())) {
            paginationInfo = paginationUtils.getPaginationInfoObject();
        } else {
            paginationInfo = searchDTO.getPaginationInfo();
        }
        paginationInfo.setTotalItems(count);

        // DATA QUERY
        StringBuffer sqlQuery = new StringBuffer();
        sqlQuery.append("SELECT esr.id AS empSwpReqId, DATE_FORMAT(esr.swp_date, '%d/%m/%Y') AS swipeDate, DATE_FORMAT(esr.created_date_time, '%d/%m/%Y') AS applicationDate, ");
        sqlQuery.append("esr.day_type AS dayType, esr.direction AS swipeDirection, esr.req_in_time AS reqInTime, esr.req_out_time AS reqOutTime, ");
        sqlQuery.append("esr.status AS status, esr.remark AS remark, ");
        // Subquery to get the last approver's remark
        sqlQuery.append("(SELECT sra.remark FROM swp_rqst_aprvr_dtl AS sra ");
        sqlQuery.append("WHERE sra.swp_rqst_id = esr.id AND sra.remark IS NOT NULL ORDER BY sra.sequence DESC LIMIT 1) AS lastApproverRemark ");
        sqlQuery.append("FROM emp_swp_rqst AS esr ");
        sqlQuery.append("INNER JOIN emp_dtls AS ed ON ed.id = esr.emp_id ");
        sqlQuery.append(whereClause);
        sqlQuery.append(paginationUtils.getSortingQuery(columns, paginationInfo));
        sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));

        // EXECUTE DATA QUERY
        Query dataQuery = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);
        setQueryParameters(dataQuery, searchDTO);

        List<Tuple> results = dataQuery.getResultList();

        List<Object> dataList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(results)) {
            for (Tuple result : results) {
                EmployeeSwipeRequestDashboardDTO dashboardDTO = EmployeeSwipeRequestDashboardDTO.builder()
                        .empSwpReqId(result.get("empSwpReqId", String.class))
                        .swipeDate(result.get("swipeDate", String.class))
                        .applicationDate(result.get("applicationDate", String.class))
                        .dayType(EnumConverter.toDisplayString(result.get("dayType", String.class), DayType.class))
                        .swipeDirection(EnumConverter.toDisplayString(result.get("swipeDirection", String.class), SwipeDirection.class))
                        .reqInTime(TimeHelper.convertToString(convertSqlTimeToLocalTime(result.get("reqInTime"))))
                        .reqOutTime(TimeHelper.convertToString(convertSqlTimeToLocalTime(result.get("reqOutTime"))))
                        .status(EnumConverter.toDisplayString(result.get("status", String.class), SwipeStatus.class))
                        .remark(result.get("remark", String.class))
                        .lastApproverRemark(result.get("lastApproverRemark", String.class))
                        .build();
                dataList.add(dashboardDTO);
            }
        }
        return (PaginatedDataResponse<EmployeeSwipeRequestDashboardDTO>) paginationUtils.getPaginatedResponse(columns, dataList, searchDTO.getPaginationInfo());
    }

    // Helper method to set parameters for both queries
    private void setQueryParameters(Query query, EmployeeSwipeRequestSearchDTO dto) {
        if (!ValidateUtils.isStringEmpty(dto.getReqEmpId())){
            query.setParameter("reqEmpId", dto.getReqEmpId());
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
