package com.smartcare.hrms.attendance.dao.application;

import com.smartcare.hrms.attendance.dto.application.RequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.RequestDashboardSearchDTO;
import com.smartcare.hrms.common.pagination.ColumnMeta;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.pagination.PaginationInfo;
import com.smartcare.hrms.common.pagination.PaginationUtils;
import com.smartcare.hrms.common.utility.DateHelper;
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
public class RequestDashboardDAO {


    private @Autowired EntityManager entityManager;
    private @Autowired PaginationUtils paginationUtils;

    private static List<ColumnMeta> columns = new ArrayList<>(List.of(
            new ColumnMeta("Application Date", "sortDate"),
            new ColumnMeta("Application Type", "applicationType"),
            new ColumnMeta("From Date", "fromDate"),
            new ColumnMeta("To Date", "toDate"),
            new ColumnMeta("Status", "status")
    ));

    public PaginatedDataResponse<RequestDashboardDTO> getData(RequestDashboardSearchDTO dto) {

        PaginationInfo paginationInfo;

        if (ValidateUtils.isNullObject(dto.getPaginationInfo())) {
            paginationInfo = paginationUtils.getPaginationInfoObject();
        } else {
            paginationInfo = dto.getPaginationInfo();
        }

        StringBuffer sqlQuery = new StringBuffer();

        sqlQuery.append("SELECT * FROM ( ");

        /* LEAVE REQUEST */
        sqlQuery.append("SELECT ");
        sqlQuery.append("lor.id AS requestId, ");
        sqlQuery.append("lor.emp_id AS empId, ");
        sqlQuery.append("DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, ");
        sqlQuery.append("'Leave Request' AS applicationType, ");
        sqlQuery.append("DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, ");
        sqlQuery.append("DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate, ");
        sqlQuery.append("lor.status AS status, ");
        sqlQuery.append("lor.reason AS reason, ");
        sqlQuery.append("lor.apprvr_rmrk AS remark, ");
        sqlQuery.append("(SELECT lord.rmrk ");
        sqlQuery.append(" FROM lv_od_rqst_dtls lord ");
        sqlQuery.append(" WHERE lord.lv_rqst_id = lor.id ");
        sqlQuery.append(" AND lord.rmrk IS NOT NULL ");
        sqlQuery.append(" ORDER BY lord.sequence DESC LIMIT 1) AS lastApproverRemark, ");
        sqlQuery.append("lor.app_dt AS sortDate ");
        sqlQuery.append("FROM lv_od_rqst lor ");
        sqlQuery.append("WHERE lor.app_type = 'LEAVE_RQST' ");

        sqlQuery.append("UNION ALL ");

        /* OD REQUEST */
        sqlQuery.append("SELECT ");
        sqlQuery.append("lor.id AS requestId, ");
        sqlQuery.append("lor.emp_id AS empId, ");
        sqlQuery.append("DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, ");
        sqlQuery.append("'OD Request' AS applicationType, ");
        sqlQuery.append("DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, ");
        sqlQuery.append("DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate, ");
        sqlQuery.append("lor.status AS status, ");
        sqlQuery.append("lor.reason AS reason, ");
        sqlQuery.append("lor.apprvr_rmrk AS remark, ");
        sqlQuery.append("(SELECT lord.rmrk ");
        sqlQuery.append(" FROM lv_od_rqst_dtls lord ");
        sqlQuery.append(" WHERE lord.lv_rqst_id = lor.id ");
        sqlQuery.append(" AND lord.rmrk IS NOT NULL ");
        sqlQuery.append(" ORDER BY lord.sequence DESC LIMIT 1) AS lastApproverRemark, ");
        sqlQuery.append("lor.app_dt AS sortDate ");
        sqlQuery.append("FROM lv_od_rqst lor ");
        sqlQuery.append("WHERE lor.app_type = 'OD_RQST' ");

        sqlQuery.append("UNION ALL ");

        /* SWIPE REQUEST */
        sqlQuery.append("SELECT ");
        sqlQuery.append("esr.id AS requestId, ");
        sqlQuery.append("esr.emp_id AS empId, ");
        sqlQuery.append("DATE_FORMAT(esr.created_date_time,'%d/%m/%Y') AS applicationDate, ");
        sqlQuery.append("'Swipe Request' AS applicationType, ");
        sqlQuery.append("DATE_FORMAT(esr.swp_date,'%d/%m/%Y') AS fromDate, ");
        sqlQuery.append("DATE_FORMAT(esr.swp_date,'%d/%m/%Y') AS toDate, ");
        sqlQuery.append("esr.status AS status, ");
        sqlQuery.append("'' AS reason, ");
        sqlQuery.append("esr.remark AS remark, ");
        sqlQuery.append("(SELECT sra.remark ");
        sqlQuery.append(" FROM swp_rqst_aprvr_dtl sra ");
        sqlQuery.append(" WHERE sra.swp_rqst_id = esr.id ");
        sqlQuery.append(" AND sra.remark IS NOT NULL ");
        sqlQuery.append(" ORDER BY sra.sequence DESC LIMIT 1) AS lastApproverRemark, ");
        sqlQuery.append("esr.created_date_time AS sortDate ");
        sqlQuery.append("FROM emp_swp_rqst esr ");

        sqlQuery.append(") dashboard ");
        sqlQuery.append("WHERE 1=1 ");

        if (!ValidateUtils.isStringEmpty(dto.getEmpId())) {
            sqlQuery.append("AND dashboard.empId = '").append(dto.getEmpId()).append("' ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getRequestType())) {
            String applicationType = null;

            switch (dto.getStatus().toUpperCase()) {
                case "LEAVE_REQ":
                    applicationType = "Leave Request";
                    break;
                case "OD_REQ":
                    applicationType = "OD Request";
                    break;
                case "SWIPE_REQ":
                    applicationType = "Swipe Request";
                    break;
            }

            if (applicationType != null) {
                sqlQuery.append("AND dashboard.applicationType = '")
                        .append(applicationType).append("' ");
            }
        }
        if(!ValidateUtils.isStringEmpty(dto.getStatus())){
            sqlQuery.append("AND dashboard.status ='").append(dto.getStatus()).append("' ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getFromDate())) {
            LocalDate fromDate = DateHelper.convertUserDateToLocalDate(dto.getFromDate());

            sqlQuery.append("AND DATE(dashboard.sortDate) >= '").append(fromDate).append("' ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getToDate())) {
            LocalDate toDate = DateHelper.convertUserDateToLocalDate(dto.getToDate());

            sqlQuery.append("AND DATE(dashboard.sortDate) <= '").append(toDate).append("' ");
        }

        String countQuery = "SELECT COUNT(*) FROM (" + sqlQuery.toString() + ") cnt";

        Integer count = ((Number) entityManager.createNativeQuery(countQuery).getSingleResult()).intValue();

        paginationInfo.setTotalItems(count);

        sqlQuery.append("ORDER BY dashboard.sortDate DESC ");
        sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));

        Query query = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);

        List<Tuple> results = query.getResultList();

        List<Object> dataList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(results)) {

            for (Tuple result : results) {

                RequestDashboardDTO dashboardDTO = RequestDashboardDTO.builder()
                        .requestId(result.get("requestId", String.class))
                        .applicationDate(result.get("applicationDate", String.class))
                        .applicationType(result.get("applicationType", String.class))
                        .fromDate(result.get("fromDate", String.class))
                        .toDate(result.get("toDate", String.class))
                        .status(result.get("status", String.class))
                        .reason(result.get("reason", String.class))
                        .remark(result.get("remark", String.class))
                        .lastApproverRemark(result.get("lastApproverRemark", String.class))
                        .build();

                dataList.add(dashboardDTO);
            }
        }

        return (PaginatedDataResponse<RequestDashboardDTO>) paginationUtils.getPaginatedResponse(columns, dataList, paginationInfo);
    }


}
