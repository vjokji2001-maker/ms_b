package com.smartcare.hrms.attendance.dao.shift;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDashboardDTO;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftSearchDTO;
import com.smartcare.hrms.attendance.dto.shift.ShiftDetailsDTO;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
public class EmployeeShiftDashboardDAO {


    private static final String countQuery = "SELECT COUNT(*) FROM emp_dtls";

    private static final String countyQueryNonAdmin=" SELECT COUNT(*) FROM emp_dtls =:empId ";

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PaginationUtils paginationUtils;

    @Autowired
    private MessageResource messageResource;


    private static final List<ColumnMeta> baseColumns = List.of(
            new ColumnMeta("Employee Name", "formalName"),
            new ColumnMeta("Employee Code", "empCode")
    );

    public PaginatedDataResponse<EmployeeShiftDashboardDTO> getData(EmployeeShiftSearchDTO dto) {

        boolean isAdmin ="Admin".equalsIgnoreCase(dto.getJobTitle());

        Integer count ;
        if(isAdmin) {
            count= ((Long) entityManager.createNativeQuery(countQuery).getSingleResult() ).intValue();
        }else {
            count= ((Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM emp_dtls WHERE id = " + dto.getEmpId()).getSingleResult()).intValue();
        }

        PaginationInfo paginationInfo;
        if (ValidateUtils.isNullObject(dto.getPaginationInfo())) {
            paginationInfo = paginationUtils.getPaginationInfoObject();
        } else {
            paginationInfo = dto.getPaginationInfo();
        }
        paginationInfo.setTotalItems(count);

        // Validate dates
        if (ValidateUtils.isStringEmpty(dto.getFromDate()) || ValidateUtils.isStringEmpty(dto.getToDate())) {
            throw new IllegalArgumentException(
                    messageResource.getEnglishMessage("from.date.and.to.date.required"));
        }

        // Generate date range
        LocalDate fromDate = LocalDate.parse(dto.getFromDate());
        LocalDate toDate = LocalDate.parse(dto.getToDate());
        List<LocalDate> dateRange = new ArrayList<>();
        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            dateRange.add(d);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        List<ColumnMeta> columns = new ArrayList<>(baseColumns);

        dateRange.forEach(d -> {
            String dateStr = d.format(formatter); // e.g. "01-06-2025"
            columns.add(new ColumnMeta(dateStr, dateStr)); // header = key = "01-06-2025"
        });

        // MAIN SQL QUERY - WRAP EMPLOYEE SELECTION IN SUBQUERY
        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("SELECT emp.id AS employeeId, emp.formal_name AS employeeName, emp.emp_code AS employeeCode, ");
        sqlQuery.append("st.id AS shiftId, st.name AS shiftName, st.code AS shiftCode, es.date AS date ");
        sqlQuery.append("FROM ( ");

        // SUBQUERY: Get paginated employees
        sqlQuery.append("SELECT ed.id, ed.formal_name, ed.emp_code ");
        sqlQuery.append("FROM emp_dtls ed ");
        sqlQuery.append("INNER JOIN company_details cd ON cd.id = ed.company_id ");
        sqlQuery.append("INNER JOIN branch_details bd ON bd.id = ed.branch_id ");
        sqlQuery.append("INNER JOIN department d ON d.id = ed.department_id ");
        sqlQuery.append("INNER JOIN designation dg ON dg.id = ed.designation_id ");
        sqlQuery.append("INNER JOIN grades g ON g.id = ed.grade_id ");
        sqlQuery.append("INNER JOIN category c ON c.id = ed.category_id ");
        sqlQuery.append("INNER JOIN wrk_loc wl ON wl.id = ed.work_location_id ");
        sqlQuery.append("WHERE 1=1 ");
        if (!isAdmin) {
            sqlQuery.append("AND ed.id = ").append(dto.getEmpId()).append(" ");
        } else {
            // Apply filters inside subquery
            if (!ValidateUtils.isStringEmpty(dto.getName())) {
                sqlQuery.append("AND ed.formal_name LIKE '%")
                        .append(dto.getName().trim().replace("'", "''"))
                        .append("%' ");
            }
            if (!ValidateUtils.isStringEmpty(dto.getCode())) {
                sqlQuery.append("AND ed.emp_code = '")
                        .append(dto.getCode().trim().replace("'", "''"))
                        .append("' ");
            }
            if (!ValidateUtils.isLongZero(dto.getCompanyId())) {
                sqlQuery.append("AND cd.id = ").append(dto.getCompanyId()).append(" ");
            }
            if (!ValidateUtils.isLongZero(dto.getBranchId())) {
                sqlQuery.append("AND bd.id = ").append(dto.getBranchId()).append(" ");
            }
            if (!ValidateUtils.isLongZero(dto.getDepartmentId())) {
                sqlQuery.append("AND d.id = ").append(dto.getDepartmentId()).append(" ");
            }
            if (!ValidateUtils.isLongZero(dto.getDesignationId())) {
                sqlQuery.append("AND dg.id = ").append(dto.getDesignationId()).append(" ");
            }
        }
        // PAGINATION + SORTING inside subquery
        sqlQuery.append(paginationUtils.getSortingQuery(baseColumns, paginationInfo));
        sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));
        sqlQuery.append(") emp ");

        // Join shifts to the paginated employees
        sqlQuery.append("LEFT JOIN ( ")
                .append("SELECT emp_id, shift_id, date ")
                .append("FROM emp_shift ")
                .append("WHERE date BETWEEN '")
                .append(dto.getFromDate().trim().replace("'", "''"))
                .append("' AND '")
                .append(dto.getToDate().trim().replace("'", "''"))
                .append("' ")
                .append(") es ON es.emp_id = emp.id ");
        sqlQuery.append("LEFT JOIN shift_tmplt st ON st.id = es.shift_id ");

        // Sorting by employee name
        sqlQuery.append("ORDER BY emp.formal_name ");
        if (paginationInfo.getDataSorting() != null) {
            sqlQuery.append(paginationInfo.getDataSorting().getSortingOrder());
        }

        Query nativeQuery = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);
        List<Tuple> results = nativeQuery.getResultList();

        Map<String, EmployeeShiftDashboardDTO> employeeMap = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            String empId = tuple.get("employeeId", String.class);
            if (empId == null) continue;

            EmployeeShiftDashboardDTO empDTO = employeeMap.get(empId);
            if (empDTO == null) {
                empDTO = EmployeeShiftDashboardDTO.builder()
                        .employeeId(empId)
                        .employeeName(tuple.get("employeeName", String.class))
                        .employeeCode(tuple.get("employeeCode", String.class))
                        .empShiftList(new ArrayList<>())
                        .build();
                employeeMap.put(empId, empDTO);
            }

            // Add shift details if available
            if (tuple.get("shiftId") != null) {
                java.sql.Date sqlDate = tuple.get("date", java.sql.Date.class);


                String dateStr = sqlDate != null
                        ? sqlDate.toLocalDate().format(formatter)
                        : null;

                ShiftDetailsDTO shiftDTO = ShiftDetailsDTO.builder()
                        .shiftId(String.valueOf(tuple.get("shiftId", Long.class)))
                        .shiftCode(tuple.get("shiftCode", String.class))
                        .shiftName(tuple.get("shiftName", String.class))
                        .date(dateStr)
                        .build();

                empDTO.getEmpShiftList().add(shiftDTO);
            }
        }

        // RETURN PAGINATED RESPONSE — exactly your original logic
        List<EmployeeShiftDashboardDTO> finalList = new ArrayList<>(employeeMap.values());
        return (PaginatedDataResponse<EmployeeShiftDashboardDTO>)
                paginationUtils.getPaginatedResponse(
                        columns,
                        Collections.singletonList(finalList),
                        paginationInfo
                );
    }
}