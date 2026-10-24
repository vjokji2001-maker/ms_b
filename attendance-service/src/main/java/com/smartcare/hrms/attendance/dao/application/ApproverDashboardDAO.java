package com.smartcare.hrms.attendance.dao.application;

import com.smartcare.hrms.attendance.dto.application.ApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.ApproverDashboardSearchDTO;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
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

@Repository
public class ApproverDashboardDAO {

    private @Autowired EntityManager entityManager;

    private @Autowired PaginationUtils paginationUtils;

    public static final String TYPE_LEAVE="LEAVE";
    public static final String TYPE_OD="OD";
    public static final String TYPE_SWIPE="SWIPE";

    private static final List<ColumnMeta> columns= new ArrayList<>(List.of(
            new ColumnMeta("Emp Name",        "name"),
            new ColumnMeta("Emp Code",        "code"),
            new ColumnMeta("Request Type",    "requestType"),
            new ColumnMeta("App Date",        "applicationDate"),
            new ColumnMeta("Leave Name",      "leaveName"),
            new ColumnMeta("From Date",       "fromDate"),
            new ColumnMeta("Duration",        "fromDateDuration"),
            new ColumnMeta("To Date",         "toDate"),
            new ColumnMeta("Duration",        "toDateDuration"),
            new ColumnMeta("Swipe Date",      "swipeDate"),
            new ColumnMeta("Day Type",        "dayType"),
            new ColumnMeta("Swipe Direction", "swipeDirection"),
            new ColumnMeta("Shift Code",      "shiftCode"),
            new ColumnMeta("Req In Time",     "reqInTime"),
            new ColumnMeta("Req Out Time",    "reqOutTime"),
            new ColumnMeta("Rqst Reason",     "reason"),
            new ColumnMeta("Status",          "status"),
            new ColumnMeta("Remark",          "remark"),
            new ColumnMeta("Last Remark",     "lastApproverRemark")
    ));

    public PaginatedDataResponse<ApproverDashboardDTO> getData(ApproverDashboardSearchDTO dto){
        boolean includeLeave = isTypeIncluded(dto.getRequestType(), TYPE_LEAVE);
        boolean includeOD    = isTypeIncluded(dto.getRequestType(), TYPE_OD);
        boolean includeSwipe = isTypeIncluded(dto.getRequestType(), TYPE_SWIPE);

        // ---- Count query (UNION of applicable sub-counts) -------------------
        List<String> countParts = new ArrayList<>();
        if (includeLeave) countParts.add(buildLeaveCountQuery(dto));
        if (includeOD)    countParts.add(buildODCountQuery(dto));
        if (includeSwipe) countParts.add(buildSwipeCountQuery(dto));

        String totalCountSql = "SELECT SUM(cnt) FROM (" +
                String.join(" UNION ALL ", countParts) + ") AS combined_count";

        int count = ((Number) entityManager.createNativeQuery(totalCountSql).getSingleResult()).intValue();

        // ---- Pagination setup -----------------------------------------------
        PaginationInfo paginationInfo = ValidateUtils.isNullObject(dto.getPaginationInfo())
                ? paginationUtils.getPaginationInfoObject()
                : dto.getPaginationInfo();
        paginationInfo.setTotalItems(count);

        // ---- Data query (UNION ALL of applicable sub-selects) ---------------
        List<String> dataParts = new ArrayList<>();
        if (includeLeave) dataParts.add(buildLeaveDataQuery(dto));
        if (includeOD)    dataParts.add(buildODDataQuery(dto));
        if (includeSwipe) dataParts.add(buildSwipeDataQuery(dto));

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

        return (PaginatedDataResponse<ApproverDashboardDTO>)
                paginationUtils.getPaginatedResponse(columns, dataList, dto.getPaginationInfo());
    }

    private String buildLeaveCountQuery(ApproverDashboardSearchDTO dto){
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
                .append("WHERE lor.app_type = 'LEAVE_RQST' ");
        appendCommonLeaveODFilters(sb, dto);
        appendStatusFilter(sb, dto, "lor.status");
        return sb.toString();
    }
    private String buildODCountQuery(ApproverDashboardSearchDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) AS cnt FROM lv_od_rqst lor ")
                .append("INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id = lor.id ")
                .append("  AND lord.aprvr_id = '").append(dto.getApproverEmpId()).append("' ")
                .append("  AND lord.status IS NOT NULL ")
                .append("INNER JOIN emp_dtls ed ON ed.id = lor.emp_id ")
                .append("WHERE lor.app_type = 'OD_RQST' ");
        appendCommonLeaveODFilters(sb, dto);
        appendStatusFilter(sb, dto, "lor.status");
        return sb.toString();
    }

    private String buildSwipeCountQuery(ApproverDashboardSearchDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) AS cnt FROM emp_swp_rqst esr ")
                .append("INNER JOIN swp_rqst_aprvr_dtl sra ON sra.swp_rqst_id = esr.id ")
                .append("  AND sra.approver_id = '").append(dto.getApproverEmpId()).append("' ")
                .append("  AND sra.status IS NOT NULL ")
                .append("LEFT JOIN emp_shift es ON es.id = esr.emp_shift_id ")
                .append("LEFT JOIN shift_tmplt st ON st.id = es.shift_id ")
                .append("INNER JOIN emp_dtls ed ON ed.id = esr.emp_id ")
                .append("INNER JOIN company_details c ON c.id = ed.company_id ")
                .append("INNER JOIN branch_details b ON b.id = ed.branch_id ")
                .append("INNER JOIN department dp ON dp.id = ed.department_id ")
                .append("INNER JOIN designation dg ON dg.id = ed.designation_id ")
                .append("INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id ")
                .append("INNER JOIN category ct ON ct.id = ed.category_id ")
                .append("INNER JOIN grades g ON g.id = ed.grade_id ")
                .append("WHERE 1=1 ");
        appendCommonSwipeFilters(sb, dto);
        appendStatusFilter(sb, dto, "sra.status");
        return sb.toString();
    }

    private String buildLeaveDataQuery(ApproverDashboardSearchDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append("  lord.id AS recordId, ")
                .append("  'LEAVE' AS requestType, ")
                .append("  ed.formal_name AS name, ")
                .append("  ed.emp_code AS code, ")
                .append("  DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, ")
                .append("  ln.name AS leaveName, ")
                .append("  DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, ")
                .append("  lor.frm_dt_typ AS fromDateDuration, ")
                .append("  DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate, ")
                .append("  lor.to_dt_typ AS toDateDuration, ")
                .append("  NULL AS swipeDate, ")
                .append("  NULL AS dayType, ")
                .append("  NULL AS swipeDirection, ")
                .append("  NULL AS shiftCode, ")
                .append("  NULL AS reqInTime, ")
                .append("  NULL AS reqOutTime, ")
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
                .append("WHERE lor.app_type = 'LEAVE_RQST' ");
        appendCommonLeaveODFilters(sb, dto);
        appendStatusFilter(sb, dto, "lor.status");
        return sb.toString();
    }

    private String buildODDataQuery(ApproverDashboardSearchDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append("  lord.id AS recordId, ")
                .append("  'OD' AS requestType, ")
                .append("  ed.formal_name AS name, ")
                .append("  ed.emp_code AS code, ")
                .append("  DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, ")
                .append("  NULL AS leaveName, ")
                .append("  DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, ")
                .append("  lor.frm_dt_typ AS fromDateDuration, ")
                .append("  DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate, ")
                .append("  lor.to_dt_typ AS toDateDuration, ")
                .append("  NULL AS swipeDate, ")
                .append("  NULL AS dayType, ")
                .append("  NULL AS swipeDirection, ")
                .append("  NULL AS shiftCode, ")
                .append("  NULL AS reqInTime, ")
                .append("  NULL AS reqOutTime, ")
                .append("  lor.reason AS reason, ")
                .append("  lor.status AS rawStatus, ")
                .append("  IFNULL(lord.rmrk,'') AS remark, ")
                .append("  IFNULL((SELECT lord1.rmrk FROM lv_od_rqst_dtls lord1 ")
                .append("          WHERE lord1.lv_rqst_id = lor.id ")
                .append("          ORDER BY lord1.sequence DESC LIMIT 1),'') AS lastApproverRemark, ")
                .append("  lor.id AS sourceRequestId ")
                .append("FROM lv_od_rqst lor ")
                .append("INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id = lor.id ")
                .append("  AND lord.aprvr_id = '").append(dto.getApproverEmpId()).append("' ")
                .append("  AND lord.status IS NOT NULL ")
                .append("INNER JOIN emp_dtls ed ON ed.id = lor.emp_id ")
                .append("WHERE lor.app_type = 'OD_RQST' ");
        appendCommonLeaveODFilters(sb, dto);
        appendStatusFilter(sb, dto, "lor.status");
        return sb.toString();
    }

    private String buildSwipeDataQuery(ApproverDashboardSearchDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append("  sra.id AS recordId, ")
                .append("  'SWIPE' AS requestType, ")
                .append("  ed.formal_name AS name, ")
                .append("  ed.emp_code AS code, ")
                .append("  DATE_FORMAT(esr.created_date_time,'%d/%m/%Y') AS applicationDate, ")
                .append("  NULL AS leaveName, ")
                .append("  NULL AS fromDate, ")
                .append("  NULL AS fromDateDuration, ")
                .append("  NULL AS toDate, ")
                .append("  NULL AS toDateDuration, ")
                .append("  DATE_FORMAT(esr.swp_date,'%d/%m/%Y') AS swipeDate, ")
                .append("  esr.day_type AS dayType, ")
                .append("  esr.direction AS swipeDirection, ")
                .append("  st.code AS shiftCode, ")
                .append("  esr.req_in_time AS reqInTime, ")
                .append("  esr.req_out_time AS reqOutTime, ")
                .append("  NULL AS reason, ")
                .append("  sra.status AS rawStatus, ")
                .append("  sra.remark AS remark, ")
                .append("  (SELECT sra2.remark FROM swp_rqst_aprvr_dtl sra2 ")
                .append("   WHERE sra2.swp_rqst_id = esr.id AND sra2.remark IS NOT NULL ")
                .append("   ORDER BY sra2.sequence DESC LIMIT 1) AS lastApproverRemark, ")
                .append("  esr.id AS sourceRequestId ")
                .append("FROM emp_swp_rqst esr ")
                .append("INNER JOIN swp_rqst_aprvr_dtl sra ON sra.swp_rqst_id = esr.id ")
                .append("  AND sra.approver_id = '").append(dto.getApproverEmpId()).append("' ")
                .append("  AND sra.status IS NOT NULL ")
                .append("LEFT JOIN emp_shift es ON es.id = esr.emp_shift_id ")
                .append("LEFT JOIN shift_tmplt st ON st.id = es.shift_id ")
                .append("INNER JOIN emp_dtls ed ON ed.id = esr.emp_id ")
                .append("INNER JOIN company_details c ON c.id = ed.company_id ")
                .append("INNER JOIN branch_details b ON b.id = ed.branch_id ")
                .append("INNER JOIN department dp ON dp.id = ed.department_id ")
                .append("INNER JOIN designation dg ON dg.id = ed.designation_id ")
                .append("INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id ")
                .append("INNER JOIN category ct ON ct.id = ed.category_id ")
                .append("INNER JOIN grades g ON g.id = ed.grade_id ")
                .append("WHERE 1=1 ");
        appendCommonSwipeFilters(sb, dto);
        appendStatusFilter(sb, dto, "sra.status");
        return sb.toString();
    }

    private void appendCommonLeaveODFilters(StringBuilder sb,ApproverDashboardSearchDTO dto){
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
    }

    private void appendCommonSwipeFilters(StringBuilder sb, ApproverDashboardSearchDTO dto) {
        if (!ValidateUtils.isStringEmpty(dto.getName())) {
            sb.append(" AND ed.formal_name LIKE '%")
                    .append(dto.getName().trim().replace("'", "''")).append("%' ");
        }
        if (!ValidateUtils.isStringEmpty(dto.getCode())) {
            sb.append(" AND ed.emp_code = '")
                    .append(dto.getCode().trim().replace("'", "''")).append("' ");
        }
        if (!ValidateUtils.isNullObject(dto.getCompanyId())) {
            sb.append(" AND ed.company_id = ").append(dto.getCompanyId()).append(" ");
        }
        if (!ValidateUtils.isNullObject(dto.getBranchId())) {
            sb.append(" AND ed.branch_id = ").append(dto.getBranchId()).append(" ");
        }
        if (!ValidateUtils.isNullObject(dto.getDepartmentId())) {
            sb.append(" AND ed.department_id = ").append(dto.getDepartmentId()).append(" ");
        }
        if (!ValidateUtils.isNullObject(dto.getDesignationId())) {
            sb.append(" AND ed.designation_id = ").append(dto.getDesignationId()).append(" ");
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
        // Swipe uses swp_date for date range, not app_dt
        if (!ValidateUtils.isStringEmpty(dto.getFromDate())) {
            LocalDate fromDate = DateHelper.convertUserDateToLocalDate(dto.getFromDate());
            sb.append(" AND esr.swp_date >= '").append(fromDate).append("' ");
        }
        if (!ValidateUtils.isStringEmpty(dto.getToDate())) {
            LocalDate toDate = DateHelper.convertUserDateToLocalDate(dto.getToDate());
            sb.append(" AND esr.swp_date <= '").append(toDate).append("' ");
        }
    }
    private void appendStatusFilter(StringBuilder sb , ApproverDashboardSearchDTO dto, String statusColumn){
        if(!ValidateUtils.isStringEmpty(dto.getStatus())){
            sb.append(" AND ").append(statusColumn).append(" = '")
                    .append(dto.getStatus().trim().replace("'","''")).append("' ");
        }
    }

    private ApproverDashboardDTO mapTuple(Tuple result){
        String requestType=result.get("requestType", String.class);


        ApproverDashboardDTO.ApproverDashboardDTOBuilder builder= ApproverDashboardDTO.builder()
                .recordId(result.get("recordId", Long.class))
                .requestType(requestType)
                .name(result.get("name", String.class))
                .code(result.get("code", String.class))
                .applicationDate(result.get("applicationDate", String.class))
                .remark(result.get("remark", String.class))
                .lastApproverRemark(result.get("lastApproverRemark", String.class))
                .sourceRequestId(result.get("sourceRequestId", String.class));

        if (TYPE_LEAVE.equals(requestType)) {
            builder.leaveName(result.get("leaveName", String.class))
                    .fromDate(result.get("fromDate", String.class))
                    .fromDateDuration(EnumConverter.toDisplayString(result.get("fromDateDuration", String.class), DayType.class))
                    .toDate(result.get("toDate", String.class))
                    .toDateDuration(EnumConverter.toDisplayString(result.get("toDateDuration", String.class), DayType.class))
                    .reason(result.get("reason", String.class))
                    .status(EnumConverter.toDisplayString(result.get("rawStatus", String.class), ApplicationStatus.class));

        } else if (TYPE_OD.equals(requestType)) {
            builder.fromDate(result.get("fromDate", String.class))
                    .fromDateDuration(EnumConverter.toDisplayString(result.get("fromDateDuration", String.class), DayType.class))
                    .toDate(result.get("toDate", String.class))
                    .toDateDuration(EnumConverter.toDisplayString(result.get("toDateDuration", String.class), DayType.class))
                    .reason(result.get("reason", String.class))
                    .status(EnumConverter.toDisplayString(result.get("rawStatus", String.class), ApplicationStatus.class));

        } else if (TYPE_SWIPE.equals(requestType)) {
            builder.swipeDate(result.get("swipeDate", String.class))
                    .dayType(EnumConverter.toDisplayString(result.get("dayType", String.class), DayType.class))
                    .swipeDirection(EnumConverter.toDisplayString(result.get("swipeDirection", String.class), SwipeDirection.class))
                    .shiftCode(result.get("shiftCode", String.class))
                    .reqInTime(TimeHelper.convertToString(TimeHelper.convertSqlTimeToLocalTime(result.get("reqInTime"))))
                    .reqOutTime(TimeHelper.convertToString(TimeHelper.convertSqlTimeToLocalTime(result.get("reqOutTime"))))
                    .status(EnumConverter.toDisplayString(result.get("rawStatus", String.class), SwipeStatus.class));
        }

        return builder.build();
    }
    private boolean isTypeIncluded(String requestType, String type){
        return ValidateUtils.isStringEmpty(requestType)||type.equalsIgnoreCase(requestType.trim());
    }
}
