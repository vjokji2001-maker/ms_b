package com.smartcare.hrms.payroll.dao;

import com.smartcare.hrms.common.basedao.BaseDashboardDAO;
import com.smartcare.hrms.common.dto.BaseDashboardSearchDTO;
import com.smartcare.hrms.common.pagination.ColumnMeta;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.pagination.PaginationInfo;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmployeeCompensationDashboardDTO;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EmployeeCompensationDashboardDAO extends BaseDashboardDAO {

    private final static String baseCountQuery = "SELECT COUNT(*) FROM emp_dtls ed ";

    private final static String selfCountQuery = "SELECT COUNT(*) FROM  emp_dtls ed WHERE ed.id=:empId ";

    private final String GET_EMP_COMPENSATION_DASHBOARD_QUERY = "SELECT ed.id AS employeeId,ed.formal_name AS name,ed.emp_code AS code,gd.name AS gradeName,\n" +
            "COALESCE(ech.gross_amt,0.00) grossAmount,COALESCE(ech.std_deduction_amt,0.00) stdDeductionAmount,\n" +
            "COALESCE(ech.other_deduction_amt,0.00) otherDeductionAmount,COALESCE(ech.net_amt,0.00) netAmount,\n" +
            "CASE WHEN ech.id IS NULL THEN 'NOT DEFINED' ELSE 'DEFINED' END AS compensationStatus \n" +
            "FROM emp_dtls ed\n" +
            "INNER JOIN grades gd ON gd.id = ed.grade_id\n" +
            "LEFT JOIN emp_compensation_hdr ech ON ech.emp_id = ed.id";

    private static List<ColumnMeta> columns = new ArrayList<>(List.of(new ColumnMeta[]{
            new ColumnMeta("Name", "ed.formal_name"),
            new ColumnMeta("Employee Code", "ed.emp_code"),
            new ColumnMeta("Grade", "ed.gender"),
            new ColumnMeta("Gross", "ech.gross_amt"),
            new ColumnMeta("Std deductions", "ech.std_deduction_amt"),
            new ColumnMeta("Other Deductions", "ech.other_deduction_amt"),
            new ColumnMeta("Net payable", "ech.net_amt"),
            new ColumnMeta("Status", "NA")
    }));

    private PaginationInfo getPaginationInfo(BaseDashboardSearchDTO searchDTO) {

        boolean isAdmin="Admin".equalsIgnoreCase(searchDTO.getJobTitle());

        Query countQuery;
        if(isAdmin){
            countQuery = entityManager.createNativeQuery(baseCountQuery);
        }else {
            countQuery= entityManager.createNativeQuery(selfCountQuery);
            countQuery.setParameter("empId", searchDTO.getEmpId());
        }
        //setQueryParameters(countQuery, searchDTO);
        int count = ((Number) countQuery.getSingleResult()).intValue();

        PaginationInfo paginationInfo = null;
        if (ValidateUtils.isNullObject(searchDTO.getPaginationInfo())) {
            paginationInfo = paginationUtils.getPaginationInfoObject();
        } else {
            paginationInfo = searchDTO.getPaginationInfo();
        }
        paginationInfo.setTotalItems(count);
        return paginationInfo;
    }

    private String constructWhereClause(BaseDashboardSearchDTO searchDTO) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");

        boolean isAdmin="Admin".equalsIgnoreCase(searchDTO.getJobTitle());

        if(!isAdmin){
            whereClause.append(" AND ed.id=:empId ");
            return whereClause.toString();
        }

        if (!ValidateUtils.isStringEmpty(searchDTO.getName()))
            whereClause.append(" AND LOWER(ed.formal_name) LIKE LOWER(CONCAT('%', :name, '%')) ");
        if (!ValidateUtils.isStringEmpty(searchDTO.getCode()))
            whereClause.append(" AND LOWER(ed.emp_code) = LOWER(:code) ");
        if (!ValidateUtils.isNullObject(searchDTO.getCompanyId()))
            whereClause.append(" AND ed.company_id = :companyId ");
        if (!ValidateUtils.isNullObject(searchDTO.getBranchId()))
            whereClause.append(" AND ed.branch_id = :branchId ");
        if (!ValidateUtils.isNullObject(searchDTO.getDepartmentId()))
            whereClause.append(" AND ed.department_id = :departmentId ");
        if (!ValidateUtils.isNullObject(searchDTO.getDesignationId()))
            whereClause.append(" AND ed.designation_id = :designationId ");
        if (!ValidateUtils.isNullObject(searchDTO.getWorkLocationId()))
            whereClause.append(" AND ed.work_location_id = :workLocationId ");
        if (!ValidateUtils.isNullObject(searchDTO.getCategoryId()))
            whereClause.append(" AND ed.category_id = :categoryId ");
        if (!ValidateUtils.isNullObject(searchDTO.getGradeId()))
            whereClause.append(" AND ed.grade_id = :gradeId ");
        return whereClause.toString();
    }

    // Helper method to set parameters for both queries
    private void setQueryParameters(Query query, BaseDashboardSearchDTO dto) {

        boolean isAdmin ="Admin".equalsIgnoreCase(dto.getJobTitle());
        if(!isAdmin){
            query.setParameter("empId",dto.getEmpId());
            return;
        }
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
    }

    public PaginatedDataResponse<EmployeeCompensationDashboardDTO> getPayrollSummaryDashboard(BaseDashboardSearchDTO searchDTO) {
        // Use Locale.ENGLISH to ensure months like MARCH are recognized correctly
        // Parse to YearMonth

        PaginationInfo paginationInfo = getPaginationInfo(searchDTO);
        searchDTO.setPaginationInfo(paginationInfo);
        StringBuilder sqlQuery = new StringBuilder(GET_EMP_COMPENSATION_DASHBOARD_QUERY);
        String whereClause = constructWhereClause(searchDTO);
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
                EmployeeCompensationDashboardDTO dashboardDTO = EmployeeCompensationDashboardDTO.builder()
                        .employeeId(result.get("employeeId", String.class))
                        .name(result.get("name", String.class))
                        .code(result.get("code", String.class))
                        .gradeName(result.get("gradeName", String.class))
                        .grossAmount(result.get("grossAmount", BigDecimal.class))
                        .standardDeductionAmount(result.get("stdDeductionAmount", BigDecimal.class))
                        .otherDeductionAmount(result.get("otherDeductionAmount", BigDecimal.class))
                        .netAmount(result.get("netAmount", BigDecimal.class))
                        .compensationStatus(result.get("compensationStatus", String.class))
                        .build();

                dataList.add(dashboardDTO);
            }
        }
        return (PaginatedDataResponse<EmployeeCompensationDashboardDTO>) paginationUtils.getPaginatedResponse(columns, dataList, searchDTO.getPaginationInfo());
    }

}
