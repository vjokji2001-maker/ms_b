package com.smartcare.hrms.attendance.dao.application;

import com.smartcare.hrms.attendance.dto.application.CancelApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.CancelApproverSearchDTO;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.enumeration.ApplicationType;
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
public class CancelApproverDashboardDAO {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PaginationUtils paginationUtils;

    public static final String TYPE_LEAVE_CANCEL = "LEAVE_CANCEL";
    public static final String TYPE_OD_CANCEL    = "OD_CANCEL";

    private static final List<ColumnMeta> columns = new ArrayList<>(List.of(
            new ColumnMeta("Emp Name",   "name"),
            new ColumnMeta("Emp Code",   "code"),
            new ColumnMeta("App Type",   "applicationType"),
            new ColumnMeta("Leave Name", "leaveName"),
            new ColumnMeta("App Date",   "applicationDate"),
            new ColumnMeta("From Date",  "fromDate"),
            new ColumnMeta("Duration",   "fromDateDuration"),
            new ColumnMeta("To Date",    "toDate"),
            new ColumnMeta("Duration",   "toDateDuration"),
            new ColumnMeta("Rqst Reason","reason"),
            new ColumnMeta("Status",     "status"),
            new ColumnMeta("Remark",     "remark"),
            new ColumnMeta("Last Remark","lastApproverRemark")
    ));



    public PaginatedDataResponse<CancelApproverDashboardDTO> getData(CancelApproverSearchDTO dto) {

        boolean includeLeaveCancel = isTypeIncluded(dto.getRequestType(), TYPE_LEAVE_CANCEL);
        boolean includeODCancel    = isTypeIncluded(dto.getRequestType(), TYPE_OD_CANCEL);

        // ---- Count query ----------------------------------------------------
        List<String> countParts = new ArrayList<>();
        if (includeLeaveCancel) countParts.add(buildLeaveCancelCountQuery(dto));
        if (includeODCancel)    countParts.add(buildODCancelCountQuery(dto));

        String totalCountSql = "SELECT SUM(cnt) FROM (" +
                String.join(" UNION ALL ", countParts) + ") AS combined_count";

        int count = ((Number) entityManager.createNativeQuery(totalCountSql).getSingleResult()).intValue();

        // ---- Pagination setup -----------------------------------------------
        PaginationInfo paginationInfo = ValidateUtils.isNullObject(dto.getPaginationInfo())
                ? paginationUtils.getPaginationInfoObject()
                : dto.getPaginationInfo();
        paginationInfo.setTotalItems(count);

        List<String> dataParts = new ArrayList<>();
        if (includeLeaveCancel) dataParts.add(buildLeaveCancelDataQuery(dto));
        if (includeODCancel)    dataParts.add(buildODCancelDataQuery(dto));

        String dataSql = String.join(" UNION ALL ", dataParts)
                + paginationUtils.getSortingQuery(columns, paginationInfo)
                + paginationUtils.getLimitSqlString(paginationInfo);

        // ---- Execute & map --------------------------------------------------
        Query nativeQuery = entityManager.createNativeQuery(dataSql, Tuple.class);
        List<Tuple> results = nativeQuery.getResultList();

        List<Object> dataList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(results)) {
            for (Tuple result : results) {
                dataList.add(mapTuple(result));
            }
        }

        return (PaginatedDataResponse <CancelApproverDashboardDTO>)
                paginationUtils.getPaginatedResponse(columns, dataList, dto.getPaginationInfo());
    }


    private String buildLeaveCancelCountQuery(CancelApproverSearchDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) AS cnt FROM lv_od_rqst lor ")
                .append("INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id = lor.id ")
                .append("  AND lord.aprvr_id = '").append(dto.getApproverEmpId()).append("' ")
                .append("  AND lord.status IS NOT NULL ")
                .append("INNER JOIN emp_dtls ed ON ed.id = lor.emp_id ")
                .append("INNER JOIN company_details c ON c.id = ed.company_id ")
                .append("INNER JOIN branch_details b ON b.id = ed.branch_id ")
                .append("INNER JOIN department dp ON dp.id = ed.department_id ")
                .append("INNER JOIN designation dg ON dg.id = ed.designation_id ")
                .append("INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id ")
                .append("INNER JOIN category ct ON ct.id = ed.category_id ")
                .append("INNER JOIN grades g ON g.id = ed.grade_id ")
                .append("WHERE lor.app_type = 'LEAVE_CANCEL' ");
        appendCommonFilters(sb, dto);
        return sb.toString();
    }

    private String buildODCancelCountQuery(CancelApproverSearchDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) AS cnt FROM lv_od_rqst lor ")
                .append("INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id = lor.id ")
                .append("  AND lord.aprvr_id = '").append(dto.getApproverEmpId()).append("' ")
                .append("  AND lord.status IS NOT NULL ")
                .append("INNER JOIN emp_dtls ed ON ed.id = lor.emp_id ")
                .append("INNER JOIN company_details c ON c.id = ed.company_id ")
                .append("INNER JOIN branch_details b ON b.id = ed.branch_id ")
                .append("INNER JOIN department dp ON dp.id = ed.department_id ")
                .append("INNER JOIN designation dg ON dg.id = ed.designation_id ")
                .append("INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id ")
                .append("INNER JOIN category ct ON ct.id = ed.category_id ")
                .append("INNER JOIN grades g ON g.id = ed.grade_id ")
                .append("WHERE lor.app_type = 'OD_CANCEL' ");
        appendCommonFilters(sb, dto);
        return sb.toString();
    }


    private String buildLeaveCancelDataQuery(CancelApproverSearchDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append("  lord.id AS recordId, ")
                .append("  'LEAVE_CANCEL' AS cancelType, ")
                .append("  ed.formal_name AS name, ")
                .append("  ed.emp_code AS code, ")
                .append("  lor.app_type AS applicationType, ")
                .append("  ln.name AS leaveName, ")
                .append("  DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, ")
                .append("  DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, ")
                .append("  lor.frm_dt_typ AS fromDateDuration, ")
                .append("  DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate, ")
                .append("  lor.to_dt_typ AS toDateDuration, ")
                .append("  lor.reason AS reason, ")
                .append("  lor.status AS rawStatus, ")
                .append("  lord.rmrk AS remark, ")
                .append("  (SELECT lord1.rmrk FROM lv_od_rqst_dtls lord1 ")
                .append("   WHERE lord1.lv_rqst_id = lor.id AND lord1.rmrk IS NOT NULL ")
                .append("   ORDER BY lord1.sequence DESC LIMIT 1) AS lastApproverRemark, ")
                .append("  lor.id AS sourceRequestId ")
                .append("FROM lv_od_rqst lor ")
                .append("LEFT JOIN leave_name ln ON ln.id = lor.lv_nm_id ")
                .append("INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id = lor.id ")
                .append("  AND lord.aprvr_id = '").append(dto.getApproverEmpId()).append("' ")
                .append("  AND lord.status IS NOT NULL ")
                .append("INNER JOIN emp_dtls ed ON ed.id = lor.emp_id ")
                .append("INNER JOIN company_details c ON c.id = ed.company_id ")
                .append("INNER JOIN branch_details b ON b.id = ed.branch_id ")
                .append("INNER JOIN department dp ON dp.id = ed.department_id ")
                .append("INNER JOIN designation dg ON dg.id = ed.designation_id ")
                .append("INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id ")
                .append("INNER JOIN category ct ON ct.id = ed.category_id ")
                .append("INNER JOIN grades g ON g.id = ed.grade_id ")
                .append("WHERE lor.app_type = 'LEAVE_CANCEL' ");
        appendCommonFilters(sb, dto);
        return sb.toString();
    }

    private String buildODCancelDataQuery(CancelApproverSearchDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append("  lord.id AS recordId, ")
                .append("  'OD_CANCEL' AS cancelType, ")
                .append("  ed.formal_name AS name, ")
                .append("  ed.emp_code AS code, ")
                .append("  lor.app_type AS applicationType, ")
                .append("  NULL AS leaveName, ")
                .append("  DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, ")
                .append("  DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, ")
                .append("  lor.frm_dt_typ AS fromDateDuration, ")
                .append("  DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate, ")
                .append("  lor.to_dt_typ AS toDateDuration, ")
                .append("  lor.reason AS reason, ")
                .append("  lor.status AS rawStatus, ")
                .append("  lord.rmrk AS remark, ")
                .append("  (SELECT lord1.rmrk FROM lv_od_rqst_dtls lord1 ")
                .append("   WHERE lord1.lv_rqst_id = lor.id AND lord1.rmrk IS NOT NULL ")
                .append("   ORDER BY lord1.sequence DESC LIMIT 1) AS lastApproverRemark, ")
                .append("  lor.id AS sourceRequestId ")
                .append("FROM lv_od_rqst lor ")
                .append("INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id = lor.id ")
                .append("  AND lord.aprvr_id = '").append(dto.getApproverEmpId()).append("' ")
                .append("  AND lord.status IS NOT NULL ")
                .append("INNER JOIN emp_dtls ed ON ed.id = lor.emp_id ")
                .append("INNER JOIN company_details c ON c.id = ed.company_id ")
                .append("INNER JOIN branch_details b ON b.id = ed.branch_id ")
                .append("INNER JOIN department dp ON dp.id = ed.department_id ")
                .append("INNER JOIN designation dg ON dg.id = ed.designation_id ")
                .append("INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id ")
                .append("INNER JOIN category ct ON ct.id = ed.category_id ")
                .append("INNER JOIN grades g ON g.id = ed.grade_id ")
                .append("WHERE lor.app_type = 'OD_CANCEL' ");
        appendCommonFilters(sb, dto);
        return sb.toString();
    }


    private void appendCommonFilters(StringBuilder sb, CancelApproverSearchDTO dto) {
        if (!ValidateUtils.isStringEmpty(dto.getName())) {
            sb.append(" AND ed.formal_name LIKE '%")
                    .append(dto.getName().trim().replace("'", "''")).append("%' ");
        }
        if (!ValidateUtils.isStringEmpty(dto.getCode())) {
            sb.append(" AND ed.emp_code = '")
                    .append(dto.getCode().trim().replace("'", "''")).append("' ");
        }
        if (!ValidateUtils.isLongZero(dto.getCompanyId())) {
            sb.append(" AND c.id = ").append(dto.getCompanyId()).append(" ");
        }
        if (!ValidateUtils.isLongZero(dto.getBranchId())) {
            sb.append(" AND b.id = ").append(dto.getBranchId()).append(" ");
        }
        if (!ValidateUtils.isLongZero(dto.getDepartmentId())) {
            sb.append(" AND dp.id = ").append(dto.getDepartmentId()).append(" ");
        }
        if (!ValidateUtils.isLongZero(dto.getDesignationId())) {
            sb.append(" AND dg.id = ").append(dto.getDesignationId()).append(" ");
        }
        if (!ValidateUtils.isNullObject(dto.getWorkLocationId())) {
            sb.append(" AND ed.work_location_id = ").append(dto.getWorkLocationId()).append(" ");
        }
        if (!ValidateUtils.isNullObject(dto.getCategoryId())) {
            sb.append(" AND ed.category_id = ").append(dto.getCategoryId()).append(" ");
        }
        if (!ValidateUtils.isNullObject(dto.getGradeId())) {
            sb.append(" AND ed.grade_id = ").append(dto.getGradeId()).append(" ");
        }
        if (!ValidateUtils.isStringEmpty(dto.getFromDate())) {
            LocalDate fromDate = DateHelper.convertUserDateToLocalDate(dto.getFromDate());
            sb.append(" AND lor.app_dt >= '").append(fromDate).append("' ");
        }
        if (!ValidateUtils.isStringEmpty(dto.getToDate())) {
            LocalDate toDate = DateHelper.convertUserDateToLocalDate(dto.getToDate());
            sb.append(" AND lor.app_dt <= '").append(toDate).append("' ");
        }
        if (!ValidateUtils.isStringEmpty(dto.getStatus())) {
            sb.append(" AND lor.status = '")
                    .append(dto.getStatus().trim().replace("'", "''")).append("' ");
        }
    }



    private CancelApproverDashboardDTO mapTuple(Tuple result) {
        String cancelType = result.get("cancelType", String.class);

        return CancelApproverDashboardDTO.builder()
                .recordId(result.get("recordId", Long.class))
                .cancelType(cancelType)
                .name(result.get("name", String.class))
                .code(result.get("code", String.class))
                .applicationType(EnumConverter.toDisplayString(
                        result.get("applicationType", String.class), ApplicationType.class))
                .leaveName(result.get("leaveName", String.class))   // null for OD Cancel
                .applicationDate(result.get("applicationDate", String.class))
                .fromDate(result.get("fromDate", String.class))
                .fromDateDuration(EnumConverter.toDisplayString(
                        result.get("fromDateDuration", String.class), DayType.class))
                .toDate(result.get("toDate", String.class))
                .toDateDuration(EnumConverter.toDisplayString(
                        result.get("toDateDuration", String.class), DayType.class))
                .reason(result.get("reason", String.class))
                .status(EnumConverter.toDisplayString(
                        result.get("rawStatus", String.class), ApplicationStatus.class))
                .remark(result.get("remark", String.class))
                .lastApproverRemark(result.get("lastApproverRemark", String.class))
                .sourceRequestId(result.get("sourceRequestId", String.class))
                .build();
    }

    // =========================================================================
    // UTILITY
    // =========================================================================

    private boolean isTypeIncluded(String requestType, String type) {
        return ValidateUtils.isStringEmpty(requestType)
                || type.equalsIgnoreCase(requestType.trim());
    }
}
