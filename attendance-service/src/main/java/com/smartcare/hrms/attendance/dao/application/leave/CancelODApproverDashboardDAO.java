package com.smartcare.hrms.attendance.dao.application.leave;

import com.smartcare.hrms.attendance.dto.application.OD.CancelODApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.OD.CancelODApproverSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelRequestApproverDashboardDTO;
import com.smartcare.hrms.common.enumeration.ApplicationType;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.pagination.ColumnMeta;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.pagination.PaginationInfo;
import com.smartcare.hrms.common.pagination.PaginationUtils;
import com.smartcare.hrms.common.utility.EnumConverter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CancelODApproverDashboardDAO {
    private @Autowired EntityManager entityManager;

    private @Autowired PaginationUtils paginationUtils;

    public static final String countQuery =
            "SELECT COUNT(*) FROM lv_od_rqst lor " +
                    "INNER JOIN emp_dtls ed ON ed.id = lor.emp_id " +
                    "INNER JOIN company_details c ON c.id = ed.company_id " +
                    "INNER JOIN branch_details b ON b.id = ed.branch_id " +
                    "INNER JOIN department dp ON dp.id = ed.department_id " +
                    "INNER JOIN designation dg ON dg.id = ed.designation_id " +
                    "INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id " +
                    "INNER JOIN category ct ON ct.id = ed.category_id " +
                    "INNER JOIN grades g ON g.id = ed.grade_id " +
                    "WHERE lor.app_type = 'OD_CANCEL'";

    private static List<ColumnMeta> columns = new ArrayList<>(List.of(
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
            new ColumnMeta("Remark", "lord.rmrk"),
            new ColumnMeta("Last Remark", "lastApproverRemark")
    ));

    public PaginatedDataResponse<CancelODApproverDashboardDTO> getData(CancelODApproverSearchDTO dto) {

        Integer count = ((Long) entityManager.createNativeQuery(countQuery).getSingleResult()).intValue();

        PaginationInfo paginationInfo = dto.getPaginationInfo() == null
                ? paginationUtils.getPaginationInfoObject()
                : dto.getPaginationInfo();

        paginationInfo.setTotalItems(count);

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT lord.id AS lvRqstApproveId, ");
        sql.append("ed.formal_name AS name, ed.emp_code AS code, ");
        sql.append("lor.app_type AS applicationType, ");
        sql.append("DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, ");
        sql.append("DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, ");
        sql.append("lor.frm_dt_typ AS fromDateDuration, ");
        sql.append("DATE_FORMAT(lor.to_dt,'%d/%m/%Y') AS toDate, ");
        sql.append("lor.to_dt_typ AS toDateDuration, ");
        sql.append("lor.reason AS reason, lor.status AS status, lord.rmrk AS remark, ");
        sql.append("(SELECT lord1.rmrk FROM lv_od_rqst_dtls lord1 ");
        sql.append(" WHERE lord1.lv_rqst_id = lor.id AND lord1.rmrk IS NOT NULL ");
        sql.append(" ORDER BY lord1.sequence DESC LIMIT 1) AS lastApproverRemark, ");
        sql.append("lor.id AS requestId ");

        sql.append("FROM lv_od_rqst lor ");
        sql.append("INNER JOIN lv_od_rqst_dtls lord ON lord.lv_rqst_id = lor.id ");
        sql.append("AND lord.aprvr_id = '").append(dto.getApproverEmpId()).append("' ");
        sql.append("AND lord.status IS NOT NULL ");

        sql.append("INNER JOIN emp_dtls ed ON ed.id = lor.emp_id ");
        sql.append("INNER JOIN company_details c ON c.id = ed.company_id ");
        sql.append("INNER JOIN branch_details b ON b.id = ed.branch_id ");
        sql.append("INNER JOIN department dp ON dp.id = ed.department_id ");
        sql.append("INNER JOIN designation dg ON dg.id = ed.designation_id ");
        sql.append("INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id ");
        sql.append("INNER JOIN category ct ON ct.id = ed.category_id ");
        sql.append("INNER JOIN grades g ON g.id = ed.grade_id ");

        sql.append("WHERE lor.app_type = 'OD_CANCEL' ");

        sql.append(paginationUtils.getSortingQuery(columns, paginationInfo));
        sql.append(paginationUtils.getLimitSqlString(paginationInfo));

        Query nativeQuery = entityManager.createNativeQuery(sql.toString(), Tuple.class);

        List<Tuple> results = nativeQuery.getResultList();
        List<Object> dataList = new ArrayList<>();

        for (Tuple result : results) {

            CancelRequestApproverDashboardDTO dtoObj =
                    CancelRequestApproverDashboardDTO.builder()
                            .approverId(result.get("lvRqstApproveId", Long.class))
                            .name(result.get("name", String.class))
                            .code(result.get("code", String.class))
                            .applicationType(EnumConverter.toDisplayString(result.get("applicationType", String.class), ApplicationType.class))
                            .applicationDate(result.get("applicationDate", String.class))
                            .fromDate(result.get("fromDate", String.class))
                            .fromDateDuration(EnumConverter.toDisplayString(result.get("fromDateDuration", String.class), DayType.class))
                            .toDate(result.get("toDate", String.class))
                            .toDateDuration(EnumConverter.toDisplayString(result.get("toDateDuration", String.class), DayType.class))
                            .reason(result.get("reason", String.class))
                            .status(EnumConverter.toDisplayString(result.get("status", String.class), ApplicationStatus.class))
                            .remark(result.get("lastApproverRemark", String.class))
                            .requestId(result.get("requestId", String .class))
                            .build();

            dataList.add(dtoObj);
        }

        return (PaginatedDataResponse<CancelODApproverDashboardDTO>)
                paginationUtils.getPaginatedResponse(columns, dataList, paginationInfo);
    }
}
