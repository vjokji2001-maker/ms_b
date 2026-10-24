package com.smartcare.hrms.attendance.dao.application.leave;

import com.smartcare.hrms.attendance.dto.application.OD.LeaveODApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODApproverSearchDTO;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LeaveODApproverDashboardDAO {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PaginationUtils paginationUtils;

    public static final String countQuery =
            "SELECT COUNT(*) FROM lv_od_rqst lor " +
                    "INNER JOIN emp_dtls ed ON ed.id = lor.emp_id " +
                    "WHERE lor.app_type='OD_RQST' ";

    private static List<ColumnMeta> columns = new ArrayList<>(List.of(new ColumnMeta[]{
            new ColumnMeta("Emp Name", "ed.formal_name"),
            new ColumnMeta("Emp Code", "ed.emp_code"),
            new ColumnMeta("App Type", "lor.app_type"),
            new ColumnMeta("App Date", "lor.app_dt"),
            new ColumnMeta("From Date", "lor.frm_dt"),
            new ColumnMeta("Duration", "lor.frm_dt_typ"),
            new ColumnMeta("To Date", "lor.to_dt"),
            new ColumnMeta("Duration", "lor.to_dt_typ"),
            new ColumnMeta("Rqst Reason", "lor.reason"),
            new ColumnMeta("Status", "lor.status"),
            new ColumnMeta("Remark", "remark"),
            new ColumnMeta("Last Remark", "lastApproverRemark"),
    }));

    public PaginatedDataResponse<LeaveODApproverDashboardDTO> getData(LeaveODApproverSearchDTO dto) {

        Integer count = ((Number) entityManager.createNativeQuery(countQuery).getSingleResult()).intValue();

        PaginationInfo paginationInfo = ValidateUtils.isNullObject(dto.getPaginationInfo())
                ? paginationUtils.getPaginationInfoObject()
                : dto.getPaginationInfo();

        paginationInfo.setTotalItems(count);

        StringBuilder sqlQuery = new StringBuilder();

        sqlQuery.append("SELECT lord.id AS odApproveId, ed.formal_name AS name, ed.emp_code AS code, ")
                .append("lor.app_type AS applicationType, ")
                .append("DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, ")
                .append("DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, lor.frm_dt_typ AS fromDateDuration, ")
                .append("DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate, lor.to_dt_typ AS toDateDuration, ")
                .append("lor.reason AS reason, lor.status AS status, ")
                .append("IFNULL(lord.rmrk,'') AS remark, ")
                .append("IFNULL((SELECT lord1.rmrk FROM lv_od_rqst_dtls lord1 ")
                .append(" WHERE lord1.lv_rqst_id = lor.id ")
                .append(" ORDER BY lord1.sequence DESC LIMIT 1),'') AS lastApproverRemark, ")
                .append("lor.id AS requestId ")
                .append("FROM lv_od_rqst lor ")
                .append("INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id = lor.id ")
                .append(" AND lord.aprvr_id = '").append(dto.getApproverEmpId()).append("' ")
                .append(" AND lord.status IS NOT NULL ")
                .append("INNER JOIN emp_dtls ed ON ed.id = lor.emp_id ")
                .append("WHERE lor.app_type='OD_RQST' ");

        if (!ValidateUtils.isStringEmpty(dto.getName())) {
            sqlQuery.append(" AND ed.formal_name LIKE '%").append(dto.getName().trim().replace("'", "''")).append("%'");
        }

        if (!ValidateUtils.isStringEmpty(dto.getCode())) {
            sqlQuery.append(" AND ed.emp_code = '").append(dto.getCode().trim().replace("'", "''")).append("' ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getFromDate())) {
            LocalDate fromDate = DateHelper.convertUserDateToLocalDate(dto.getFromDate());
            sqlQuery.append(" AND lor.app_dt >= '").append(fromDate).append("' ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getToDate())) {
            LocalDate toDate = DateHelper.convertUserDateToLocalDate(dto.getToDate());
            sqlQuery.append(" AND lor.app_dt <= '").append(toDate).append("' ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getStatus())) {
            sqlQuery.append(" AND lor.status = '").append(dto.getStatus()).append("' ");
        }

        sqlQuery.append(paginationUtils.getSortingQuery(columns, paginationInfo));
        sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));

        Query nativeQuery = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);

        List<Tuple> results = nativeQuery.getResultList();
        List<Object> dataList = new ArrayList<>();

        for (Tuple result : results) {
            LeaveODApproverDashboardDTO dashboardDTO = LeaveODApproverDashboardDTO.builder()
                    .approverId(result.get("odApproveId", Long.class))
                    .odRequestId(result.get("requestId", String.class))
                    .name(result.get("name", String.class))
                    .code(result.get("code", String.class))
                    .applicationType(EnumConverter.toDisplayString(result.get("applicationType", String.class), ApplicationType.class))
                    .applicationDate(result.get("applicationDate", String.class))
                    .fromDate(result.get("fromDate", String.class))
                    .fromDateDuration(EnumConverter.toDisplayString(result.get("fromDateDuration", String.class), DayType.class))
                    .toDate(result.get("toDate", String.class))
                    .toDateDuration(EnumConverter.toDisplayString(result.get("toDateDuration", String.class), DayType.class))
                    .status(EnumConverter.toDisplayString(result.get("status", String.class), ApplicationStatus.class))
                    .reason(result.get("reason", String.class))
                    .remark(result.get("remark", String.class))
                    .lastApproverRemark(result.get("lastApproverRemark", String.class))
                    .build();

            dataList.add(dashboardDTO);
        }

        return (PaginatedDataResponse<LeaveODApproverDashboardDTO>) paginationUtils.getPaginatedResponse(columns, dataList, dto.getPaginationInfo());
    }

}
