package com.smartcare.hrms.attendance.dao.application;

import com.smartcare.hrms.attendance.dto.application.CancelDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.CancelDashboardSearchDTO;
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
public class CancelDashboardDAO {
    @Autowired private EntityManager entityManager;
    @Autowired private PaginationUtils paginationUtils;

    private static List<ColumnMeta> columns = new ArrayList<>(List.of(

            new ColumnMeta("Application Date", "dashboard.sortDate"),
            new ColumnMeta("Application Type", "dashboard.applicationType"),
            new ColumnMeta("Leave Name", "dashboard.leaveName"),
            new ColumnMeta("From Date", "dashboard.fromDate"),
            new ColumnMeta("To Date", "dashboard.toDate"),
            new ColumnMeta("Status", "dashboard.status"),
            new ColumnMeta("Reason", "dashboard.reason")
    ));

    public PaginatedDataResponse<CancelDashboardDTO> getData(CancelDashboardSearchDTO dto) {

        StringBuffer sqlQuery = new StringBuffer();

        sqlQuery.append("SELECT * FROM ( ");

        /* LEAVE CANCEL */
        sqlQuery.append("SELECT ");
        sqlQuery.append("lor.id AS requestId, ");
        sqlQuery.append("lor.emp_id AS empId, ");
        sqlQuery.append("DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, ");
        sqlQuery.append("'Leave Cancel' AS applicationType, ");
        sqlQuery.append("ln.name AS leaveName, ");
        sqlQuery.append("DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, ");
        sqlQuery.append("lor.frm_dt_typ AS fromDateType, ");
        sqlQuery.append("DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate, ");
        sqlQuery.append("lor.to_dt_typ AS toDateType, ");
        sqlQuery.append("lor.ttl_cnt AS totalDays, ");
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
        sqlQuery.append("LEFT JOIN leave_name ln ON ln.id = lor.lv_nm_id ");
        sqlQuery.append("WHERE lor.app_type = 'LEAVE_CANCEL' ");

        sqlQuery.append("UNION ALL ");

        /* OD CANCEL */
        sqlQuery.append("SELECT ");
        sqlQuery.append("lor.id AS requestId, ");
        sqlQuery.append("lor.emp_id AS empId, ");
        sqlQuery.append("DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, ");
        sqlQuery.append("'OD Cancel' AS applicationType, ");
        sqlQuery.append("NULL AS leaveName, ");
        sqlQuery.append("DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, ");
        sqlQuery.append("lor.frm_dt_typ AS fromDateType, ");
        sqlQuery.append("DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate, ");
        sqlQuery.append("lor.to_dt_typ AS toDateType, ");
        sqlQuery.append("lor.ttl_cnt AS totalDays, ");
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
        sqlQuery.append("WHERE lor.app_type = 'OD_CANCEL' ");

        sqlQuery.append(") dashboard ");
        sqlQuery.append("WHERE 1=1 ");

        if (!ValidateUtils.isStringEmpty(dto.getEmpId())) {
            sqlQuery.append("AND dashboard.empId = '").append(dto.getEmpId()).append("' ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getStatus())) {
            String applicationType = null;

            switch (dto.getStatus().toUpperCase()) {
                case "LEAVE_CANCEL":
                    applicationType = "Leave Cancel";
                    break;
                case "OD_CANCEL":
                    applicationType = "OD Cancel";
                    break;
            }

            if (applicationType != null) {
                sqlQuery.append("AND dashboard.applicationType = '")
                        .append(applicationType).append("' ");
            }
        }

        if (!ValidateUtils.isStringEmpty(dto.getFromDate())) {
            LocalDate fromDate = DateHelper.convertUserDateToLocalDate(dto.getFromDate());

            sqlQuery.append("AND STR_TO_DATE(dashboard.fromDate,'%d/%m/%Y') >= '").append(fromDate).append("' ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getToDate())) {
            LocalDate toDate = DateHelper.convertUserDateToLocalDate(dto.getToDate());

            sqlQuery.append("AND STR_TO_DATE(dashboard.toDate,'%d/%m/%Y') <= '") .append(toDate).append("' ");
        }

        String countQuery = "SELECT COUNT(*) FROM (" + sqlQuery.toString() + ") cnt";

        Integer count = ((Number) entityManager.createNativeQuery(countQuery).getSingleResult()).intValue();

        PaginationInfo paginationInfo;

        if (ValidateUtils.isNullObject(dto.getPaginationInfo())) {
            paginationInfo = paginationUtils.getPaginationInfoObject();
        } else {
            paginationInfo = dto.getPaginationInfo();
        }

        paginationInfo.setTotalItems(count);

        sqlQuery.append(" ORDER BY dashboard.sortDate DESC ");
        sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));

        Query query = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);

        List<Tuple> results = query.getResultList();

        List<Object> dataList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(results)) {

            for (Tuple result : results) {

                CancelDashboardDTO dashboardDTO = CancelDashboardDTO.builder()
                        .requestId(result.get("requestId", String.class))
                        .applicationDate(result.get("applicationDate", String.class))
                        .applicationType(result.get("applicationType", String.class))
                        .leaveName(result.get("leaveName", String.class))
                        .fromDate(result.get("fromDate", String.class))
                        .fromDateDuration(result.get("fromDateType", String.class))
                        .toDate(result.get("toDate", String.class))
                        .toDateDuration(result.get("toDateType", String.class))
                        .totalDays(result.get("totalDays", Double.class))
                        .status(result.get("status", String.class))
                        .reason(result.get("reason", String.class))
                        .remark(result.get("remark", String.class))
                        .lastApproverRemark(result.get("lastApproverRemark", String.class))
                        .build();

                dataList.add(dashboardDTO);
            }
        }

        return (PaginatedDataResponse<CancelDashboardDTO>) paginationUtils.getPaginatedResponse(columns, dataList, paginationInfo);
    }
}
