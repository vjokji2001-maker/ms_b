package com.smartcare.hrms.employee.dao;

import com.smartcare.hrms.common.pagination.ColumnMeta;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.pagination.PaginationInfo;
import com.smartcare.hrms.common.pagination.PaginationUtils;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.employee.dto.EmployeeConfigDashboardDTO;
import com.smartcare.hrms.employee.dto.EmployeeConfigSearchDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EmployeeConfigDashboardDAO {

	private final static String countQuery= "SELECT COUNT(*) FROM emp_dtls";
	
	private @Autowired EntityManager entityManager;
	
	private @Autowired PaginationUtils paginationUtils;
	
	
	private static List<ColumnMeta> columns = new ArrayList<>(List.of(new ColumnMeta[] {
			
			new ColumnMeta("Name", "ed.formal_name"),
			new ColumnMeta("Employee Code", "ed.emp_code"),
			new ColumnMeta("Company ", "cd.name"),
			new ColumnMeta("Branch ", "bd.name"),
			new ColumnMeta("Department ", "d.name"),
			new ColumnMeta("Designation ", "dg.name"),
			new ColumnMeta("Grade ", "g.name"),
			new ColumnMeta("Category ", "c.name"),
			new ColumnMeta("Location ", "wl.name"),
			new ColumnMeta("WeekOfName ", "wp.name"),
			new ColumnMeta("HolidayName ", "hp.name"),
			new ColumnMeta("ApprovalGroupName ", "ag.name"),
            new ColumnMeta("LeaveTemplateName ", "lt.name")
	}));
	
	public PaginatedDataResponse<EmployeeConfigDashboardDTO> getData(EmployeeConfigSearchDTO dto){
		Integer count =((Long) entityManager.createNativeQuery(countQuery).getSingleResult()).intValue();
		
		PaginationInfo paginationInfo =null;
		if(ValidateUtils.isNullObject(dto.getPaginationInfo())) {
			paginationInfo=paginationUtils.getPaginationInfoObject();
		}else {
			paginationInfo= dto.getPaginationInfo();
		}
		paginationInfo.setTotalItems(count);
		
		StringBuffer sqlQuery = new StringBuffer();
		sqlQuery.append("SELECT ed.id AS employeeId,ed.formal_name AS employeeName, ed.emp_code AS employeeCode, ");
		sqlQuery.append("cd.name AS companyName, bd.name AS branchName, d.name AS departmentName, dg.name AS designationName ,ec.id AS employeeConfigId,  ");
		sqlQuery.append("wp.name AS weekOfName,g.name AS grade, c.name AS category,wl.name AS location, hp.name AS holidayName,  ag.name AS approvalGroupName,lt.name AS leaveTemplateName  ");
		sqlQuery.append("FROM emp_dtls ed ");
		sqlQuery.append("INNER JOIN company_details cd ON cd.id= ed.company_id ");
		sqlQuery.append("INNER JOIN branch_details bd ON bd.id= ed.branch_id ");
		sqlQuery.append("INNER JOIN department d ON d.id= ed.department_id ");
		sqlQuery.append("INNER JOIN designation  dg ON dg.id= ed.designation_id ");
		sqlQuery.append("INNER JOIN grades g ON g.id = ed.grade_id ");
		sqlQuery.append("INNER JOIN category c ON c.id=ed.category_id  ");
		sqlQuery.append("INNER JOIN wrk_loc wl ON wl.id=ed.work_location_id ");
		sqlQuery.append("LEFT JOIN emp_config ec ON ec.emp_id = ed.id ");
		sqlQuery.append("LEFT JOIN wk_off_prnt wp ON wp.id= ec.week_off_id ");
		sqlQuery.append("LEFT JOIN holiday_prnt hp ON hp.id= ec.holiday_clndr_id ");
		sqlQuery.append("LEFT JOIN aprvl_grp ag ON ag.id= ec.aprvl_grp_id ");
        sqlQuery.append("LEFT JOIN emp_leave_prnt elp ON elp.emp_id=ed.id ");
        sqlQuery.append("LEFT JOIN leave_tmplt lt ON lt.id=elp.leave_tmplt_id ");
		sqlQuery.append("WHERE 1=1 ");
		
		 
		 
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
		if(!ValidateUtils.isLongZero(dto.getWeekOfId())) {
			sqlQuery.append(" AND wp.id = ").append(dto.getWeekOfId()).append(" ");
		}
		if(!ValidateUtils.isLongZero(dto.getHolidayId())) {
			sqlQuery.append(" AND hp.id = ").append(dto.getHolidayId()).append(" ");
		}

		if(!ValidateUtils.isLongZero(dto.getApprovalGroupId())){
			sqlQuery.append(" AND ag.id = ").append(dto.getApprovalGroupId()).append(" ");
		}
        if(!ValidateUtils.isLongZero(dto.getLeaveTemplateId())){
            sqlQuery.append(" AND lt.id = ").append(dto.getLeaveTemplateId()).append(" ");
        }
		
		sqlQuery.append(paginationUtils.getSortingQuery(columns, paginationInfo));
		sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));
		
		 Query nativeQuery = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);
		
		List<Tuple> results = nativeQuery.getResultList();
		List<Object> dataList= new ArrayList<>();
		if(!CollectionUtils.isEmpty(results)) {
			for(Tuple result:results) {
				EmployeeConfigDashboardDTO dashboardDTO= EmployeeConfigDashboardDTO.builder()
						.employeeId(result.get("employeeId", String.class))
						.employeeName(result.get("employeeName", String.class))
						.employeeCode(result.get("employeeCode", String.class))
						.companyName(result.get("companyName", String.class))
						.branchName(result.get("branchName", String.class))
						.departmentName(result.get("departmentName", String.class))
						.designationName(result.get("designationName", String.class))
						.employeeConfigId(result.get("employeeConfigId")==null?null:(Long)result.get("employeeConfigId"))
						.weekOfName(result.get("weekOfName", String.class))
						.grade(result.get("grade", String.class))
						.category(result.get("category", String.class))
						.location(result.get("location", String.class))
						.holidayName(result.get("holidayName", String.class))
						.approvalGroupName(result.get("approvalGroupName", String.class))
                        .leaveTemplateName(result.get("leaveTemplateName", String.class))
						.build();
				dataList.add(dashboardDTO);
			}

		}
		return (PaginatedDataResponse<EmployeeConfigDashboardDTO>) paginationUtils.getPaginatedResponse(columns,dataList,dto.getPaginationInfo());
	}
}
