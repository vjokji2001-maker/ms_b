package com.smartcare.hrms.employee.dao;

import com.smartcare.hrms.employee.dto.EmployeeDashboardDTO;
import com.smartcare.hrms.employee.dto.EmployeeSearchDTO;
import com.smartcare.hrms.common.pagination.ColumnMeta;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.pagination.PaginationInfo;
import com.smartcare.hrms.common.pagination.PaginationUtils;
import com.smartcare.hrms.common.utility.ValidateUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EmployeeDashboardDAO {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PaginationUtils paginationUtils;


    // Base Count SQL (without WHERE)
    private final static String baseCountQuery = "SELECT COUNT(*) FROM emp_dtls ed "
            + "INNER JOIN company_details c ON c.id = ed.company_id "
            + "INNER JOIN branch_details b ON b.id = ed.branch_id "
            + "INNER JOIN department dp ON dp.id = ed.department_id "
            + "INNER JOIN designation dg ON dg.id = ed.designation_id "
            + "INNER JOIN category ct ON ct.id = ed.category_id "
            + "INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id "
            + "INNER JOIN grades g ON g.id = ed.grade_id ";


    private static List<ColumnMeta> columns = new ArrayList<>(List.of(new ColumnMeta[]{
            new ColumnMeta("Name", "ed.formal_name"),
            new ColumnMeta("Employee Code", "ed.emp_code"),
            new ColumnMeta("Email", "ed.formal_email"),
            new ColumnMeta("Contact", "ed.contact_number"),
            new ColumnMeta("Company", "c.name"),
            new ColumnMeta("Branch", "b.name"),
            new ColumnMeta("Department", "dp.name"),
            new ColumnMeta("Designation", "dg.name"),
            new ColumnMeta("Location", "loc.name"),
            new ColumnMeta("Category", "ct.name"),
            new ColumnMeta("Grade", "g.name")
    }));

    public PaginatedDataResponse<EmployeeDashboardDTO> getData(EmployeeSearchDTO searchDTO){

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");

        if (!ValidateUtils.isStringEmpty(searchDTO.getName())) {
            whereClause.append(" AND LOWER(ed.formal_name) LIKE LOWER(CONCAT('%', :name, '%')) ");
        }
        if (!ValidateUtils.isStringEmpty(searchDTO.getCode())) {
            whereClause.append(" AND LOWER(ed.emp_code) = LOWER(:code) ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getCompanyId())) {
            whereClause.append(" AND ed.company_id = :companyId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getBranchId())) {
            whereClause.append(" AND ed.branch_id = :branchId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getDepartmentId())) {
            whereClause.append(" AND ed.department_id = :departmentId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getDesignationId())) {
            whereClause.append(" AND ed.designation_id = :designationId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getWorkLocationId())) {
            whereClause.append(" AND ed.work_location_id = :workLocationId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getCategoryId())) {
            whereClause.append(" AND ed.category_id = :categoryId ");
        }
        if (!ValidateUtils.isNullObject(searchDTO.getGradeId())) {
            whereClause.append(" AND ed.grade_id = :gradeId ");
        }

        String countSql = baseCountQuery + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        setQueryParameters(countQuery, searchDTO);
        int count = ((Number) countQuery.getSingleResult()).intValue();

        PaginationInfo paginationInfo = null;
        if (ValidateUtils.isNullObject(searchDTO.getPaginationInfo())){
            paginationInfo = paginationUtils.getPaginationInfoObject();
        } else {
            paginationInfo = searchDTO.getPaginationInfo();
        }
        paginationInfo.setTotalItems(count);

        // DATA QUERY
        StringBuffer sqlQuery = new StringBuffer();
        sqlQuery.append("SELECT ed.id AS employeeId,ed.formal_name AS name,ed.emp_code AS code,ed.contact_number AS contact,ed.formal_email AS email, ");
        sqlQuery.append("c.name AS company,b.name AS branch,dp.name AS department,dg.name AS designation,ct.name AS category,loc.name AS location,g.name AS grade ");
        sqlQuery.append("FROM emp_dtls ed ");
        sqlQuery.append("INNER JOIN company_details c ON c.id = ed.company_id ");
        sqlQuery.append("INNER JOIN branch_details b ON b.id = ed.branch_id ");
        sqlQuery.append("INNER JOIN department dp ON dp.id = ed.department_id ");
        sqlQuery.append("INNER JOIN designation dg ON dg.id = ed.designation_id ");
        sqlQuery.append("INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id ");
        sqlQuery.append("INNER JOIN category ct ON ct.id = ed.category_id ");
        sqlQuery.append("INNER JOIN grades g ON g.id = ed.grade_id ");
        sqlQuery.append(whereClause);
        sqlQuery.append(paginationUtils.getSortingQuery(columns,paginationInfo));
        sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));

        // EXECUTE DATA QUERY
        Query dataQuery = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);
        setQueryParameters(dataQuery, searchDTO);

        List<Tuple> results = dataQuery.getResultList();

        List<Object> dataList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(results)){
            for (Tuple result : results) {
                EmployeeDashboardDTO dashboardDTO = EmployeeDashboardDTO.builder()
                        .employeeId(result.get("employeeId", String.class))
                        .name(result.get("name", String.class))
                        .code(result.get("code", String.class))
                        .contact(result.get("contact", String.class))
                        .email(result.get("email", String.class))
                        .company(result.get("company", String.class))
                        .branch(result.get("branch", String.class))
                        .department(result.get("department", String.class))
                        .designation(result.get("designation", String.class))
                        .location(result.get("location", String.class))
                        .category(result.get("category", String.class))
                        .grade(result.get("grade", String.class))
                        .build();
                dataList.add(dashboardDTO);
            }
        }
    return (PaginatedDataResponse<EmployeeDashboardDTO>) paginationUtils.getPaginatedResponse(columns,dataList,searchDTO.getPaginationInfo());
    }


    // Helper method to set parameters for both queries
    private void setQueryParameters(Query query, EmployeeSearchDTO dto) {
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
}
