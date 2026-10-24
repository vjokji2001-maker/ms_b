package com.smartcare.hrms.attendance.dao.application.leave;


import com.smartcare.hrms.attendance.dto.application.leave.LeaveRequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveRequestSearchDTO;
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
public class LeaveDashboardDAO {

    private @Autowired EntityManager entityManager;

    private @Autowired PaginationUtils paginationUtils;

    private static  final String baseCountQuery= "SELECT COUNT(*) FROM lv_od_rqst AS lor WHERE app_type ='LEAVE_RQST' ";

    private static List<ColumnMeta> columns = new ArrayList<>(List.of(new ColumnMeta[] {

            new ColumnMeta("App Date", "lor.app_dt"),
            new ColumnMeta("App Type", "lor.app_type"),
            new ColumnMeta("Leave Name", "ln.name"),
            new ColumnMeta("From Date ", "lor.frm_dt"),
            new ColumnMeta("Duration ", "lor.frm_dt_typ"),
            new ColumnMeta("To Date ", "lor.to_dt"),
            new ColumnMeta("Duration ", "lor.to_dt_typ"),
            new ColumnMeta("Total Days ", "lor.ttl_cnt"),
            new ColumnMeta("Reason ", "lor.reason"),
            new ColumnMeta("Status ", "lor.status"),
            new ColumnMeta("Remark ", "lord.apprvr_rmrk")
    }));

    public PaginatedDataResponse<LeaveRequestDashboardDTO> getData(LeaveRequestSearchDTO dto){
        Integer count= ((Long) entityManager.createNativeQuery(baseCountQuery).getSingleResult()).intValue();

        PaginationInfo paginationInfo=null;
        if(ValidateUtils.isNullObject(dto.getPaginationInfo())){
            paginationInfo=paginationUtils.getPaginationInfoObject();
        }else{
            paginationInfo=dto.getPaginationInfo();
        }
        paginationInfo.setTotalItems(count);

        StringBuffer sqlQuery= new StringBuffer();

        sqlQuery.append("SELECT DATE_FORMAT(lor.app_dt,'%d/%m/%Y') AS applicationDate, lor.app_type AS applicationType,ln.name AS leaveName,lor.id AS requestId, ");
        sqlQuery.append("DATE_FORMAT(lor.frm_dt,'%d/%m/%Y') AS fromDate, lor.frm_dt_typ AS fromDateType, ");
        sqlQuery.append("DATE_FORMAT(lor.to_dt,'%d/%m/%Y')  AS toDate, lor.to_dt_typ AS toDateType, ");
        sqlQuery.append("lor.status AS status, lor.ttl_cnt AS totalDays, lor.reason AS reason, ");
        sqlQuery.append("lor.apprvr_rmrk AS remark, ");
        sqlQuery.append("(SELECT lord.rmrk FROM lv_od_rqst_dtls lord ");
        sqlQuery.append("WHERE lord.lv_rqst_id = lor.id AND lord.rmrk IS NOT NULL ");
        sqlQuery.append("ORDER BY lord.sequence DESC LIMIT 1) AS lastApproverRemark ");
        sqlQuery.append("FROM lv_od_rqst lor ");
        sqlQuery.append("LEFT JOIN leave_name ln ON ln.id = lor.lv_nm_id ");
        sqlQuery.append("WHERE 1=1 ");
        sqlQuery.append("AND lor.app_type = 'LEAVE_RQST' ");

        if(!ValidateUtils.isStringEmpty(dto.getFromDate())) {
            LocalDate fromDate = DateHelper.convertUserDateToLocalDate(dto.getFromDate());
            sqlQuery.append("AND lor.app_dt >= '").append(fromDate).append("' ");
    }
        if(!ValidateUtils.isStringEmpty(dto.getToDate())) {
            LocalDate toDate = DateHelper.convertUserDateToLocalDate(dto.getToDate());
            sqlQuery.append("AND lor.app_dt <= '").append(toDate).append("' ");
        }

        if(!ValidateUtils.isStringEmpty(dto.getEmpId())) {
            sqlQuery.append("AND lor.emp_id = '").append(dto.getEmpId()).append("' ");
        }
        if (!ValidateUtils.isStringEmpty(dto.getStatus())) {
            sqlQuery.append("AND lor.status = '").append(dto.getStatus()).append("' ");
        }

        sqlQuery.append(paginationUtils.getSortingQuery(columns, paginationInfo));
        sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));

        Query nativeQuery = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);

        List<Tuple> results = nativeQuery.getResultList();
        List<Object> dataList= new ArrayList<>();
        if(!CollectionUtils.isEmpty(results)) {
            for(Tuple result:results) {
                LeaveRequestDashboardDTO dashboardDTO= LeaveRequestDashboardDTO.builder()
                        .requestId(result.get("requestId", String.class))
                        .applicationDate(result.get("applicationDate", String.class))
                        .applicationType(EnumConverter.toDisplayString(result.get("applicationType", String.class), ApplicationType.class))
                        .leaveName(result.get("leaveName", String.class))
                        .fromDate(result.get("fromDate",String.class))
                        .fromDateDuration(EnumConverter.toDisplayString(result.get("fromDateType", String.class), DayType.class))
                        .toDate(result.get("toDate", String.class))
                        .toDateDuration(EnumConverter.toDisplayString(result.get("toDateType", String.class), DayType.class))
                        .totalDays(result.get("totalDays", Double.class))
                        .reason(result.get("reason", String.class))
                        .status(EnumConverter.toDisplayString(result.get("status", String.class), ApplicationStatus.class))
                        .remark(result.get("remark", String.class))
                        .lastApproverRemark(result.get("lastApproverRemark", String.class) )
                        .build();
                dataList.add(dashboardDTO);


            }

        }
        return (PaginatedDataResponse<LeaveRequestDashboardDTO>) paginationUtils.getPaginatedResponse(columns,dataList,dto.getPaginationInfo());
    }


}
