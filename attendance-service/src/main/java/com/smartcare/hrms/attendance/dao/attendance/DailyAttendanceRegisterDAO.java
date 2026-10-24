package com.smartcare.hrms.attendance.dao.attendance;

import com.smartcare.hrms.attendance.dto.attendance.DailyAttendanceRegisterDTO;
import com.smartcare.hrms.attendance.dto.attendance.DailyAttendanceRegisterSearchDTO;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DailyAttendanceRegisterDAO {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PaginationUtils paginationUtils;

    private static final List<ColumnMeta> columns = new ArrayList<>(List.of(
            new ColumnMeta("Emp Name", "ed.formal_name"),
            new ColumnMeta("Emp Code", "ed.emp_code"),
            new ColumnMeta("Date", "ea.attendance_date"),
            new ColumnMeta("In Time", "ea.in_time"),
            new ColumnMeta("Out Time", "ea.out_time"),
            new ColumnMeta("Day Type", "ea.day_type"),
            new ColumnMeta("Portion", "ea.portion"),
            new ColumnMeta("Status", "ea.status")
    ));

    private static final String BASE_QUERY =
            " FROM emp_atndnce ea " +
                    "INNER JOIN emp_dtls ed ON ed.id = ea.employee_id " +
                    "INNER JOIN department dp ON dp.id = ed.department_id " +
                    "INNER JOIN designation dg ON dg.id = ed.designation_id "+
                    "INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id " +
                    "INNER JOIN category ct ON ct.id = ed.category_id " +
                    "INNER JOIN grades g ON g.id = ed.grade_id ";

    private String constructWhereClause(DailyAttendanceRegisterSearchDTO dto) {

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");

        boolean isAdmin= "Admin".equalsIgnoreCase(dto.getJobTitle());

        if(!isAdmin){
            where.append("AND ed.id= '").append(dto.getEmpId()).append("' ");
            return  where.toString();
        }
        if (!ValidateUtils.isStringEmpty(dto.getFromDate())) {
            where.append(" AND ea.atndnce_date >= '").append(dto.getFromDate()).append("' ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getToDate())) {
            where.append(" AND ea.atndnce_date <= '").append(dto.getToDate()).append("' ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getName())) {
            where.append(" AND LOWER(ed.formal_name) LIKE LOWER('%")
                    .append(dto.getName().trim().replace("'", "''")).append("%') ");
        }

        if (!ValidateUtils.isStringEmpty(dto.getCode())) {
            where.append(" AND LOWER(ed.emp_code) = LOWER('")
                    .append(dto.getCode().trim().replace("'", "''")).append("') ");
        }

        if (!ValidateUtils.isNullObject(dto.getCompanyId())) {
            where.append(" AND ed.company_id = ").append(dto.getCompanyId()).append(" ");
        }

        if (!ValidateUtils.isNullObject(dto.getBranchId())) {
            where.append(" AND ed.branch_id = ").append(dto.getBranchId()).append(" ");
        }

        if (!ValidateUtils.isNullObject(dto.getDepartmentId())) {
            where.append(" AND ed.department_id = ").append(dto.getDepartmentId()).append(" ");
        }

        if (!ValidateUtils.isNullObject(dto.getDesignationId())) {
            where.append(" AND ed.designation_id = ").append(dto.getDesignationId()).append(" ");
        }

        if (!ValidateUtils.isNullObject(dto.getWorkLocationId())) {
            where.append(" AND ed.work_location_id = ").append(dto.getWorkLocationId()).append(" ");
        }

        if (!ValidateUtils.isNullObject(dto.getCategoryId())) {
            where.append(" AND ed.category_id = ").append(dto.getCategoryId()).append(" ");
        }

        if (!ValidateUtils.isNullObject(dto.getGradeId())) {
            where.append(" AND ed.grade_id = ").append(dto.getGradeId()).append(" ");
        }


        return where.toString();
    }

    public PaginatedDataResponse<DailyAttendanceRegisterDTO> getSavedAttendance(
            DailyAttendanceRegisterSearchDTO dto) {

        PaginationInfo paginationInfo = ValidateUtils.isNullObject(dto.getPaginationInfo())
                ? paginationUtils.getPaginationInfoObject()
                : dto.getPaginationInfo();

        String whereClause = constructWhereClause(dto);

        String selectQuery =
                "SELECT " +
                        "ed.id AS employeeId, " +
                        "ed.emp_code AS empCode, " +
                        "ed.formal_name AS empName, " +
                        "dp.name AS department, " +
                        "dg.name AS designation, " +
                        "DATE_FORMAT(ea.atndnce_date, '%d/%m/%Y') AS attendanceDate, " +
                        "ea.in_time AS inTime, " +
                        "ea.out_time AS outTime, " +
                        "ea.day_type AS dayType, " +
                        "ea.day_type1 AS dayType1, " +
                        "ea.portion AS `portion`, " +
                        "ea.late_in_mark AS lateInMark, " +
                        "ea.early_out_mark AS earlyOutMark, " +
                        "ea.status AS status " +
                        BASE_QUERY + whereClause;

        Query countQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) " + BASE_QUERY + whereClause
        );

        int total = ((Number) countQuery.getSingleResult()).intValue();
        paginationInfo.setTotalItems(total);

        selectQuery += paginationUtils.getSortingQuery(columns, paginationInfo);
        selectQuery += paginationUtils.getLimitSqlString(paginationInfo);

        Query query = entityManager.createNativeQuery(selectQuery, Tuple.class);
        List<Tuple> results = query.getResultList();

        List<Object> dataList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(results)) {
            for (Tuple row : results) {
                dataList.add(mapToDTO(row));
            }
        }

        return (PaginatedDataResponse<DailyAttendanceRegisterDTO>)
                paginationUtils.getPaginatedResponse(columns, dataList, paginationInfo);
    }

    private DailyAttendanceRegisterDTO mapToDTO(Tuple row) {
        return DailyAttendanceRegisterDTO.builder()
                .empId(row.get("employeeId", String.class))
                .empCode(row.get("empCode", String.class))
                .empName(row.get("empName", String.class))
                .date(row.get("attendanceDate", String.class))
                .inTime(formatTime(row.get("inTime")))
                .outTime(formatTime(row.get("outTime")))
                .dayType(row.get("dayType", String.class))
                .dayType1(row.get("dayType1", String.class))
                .status(row.get("status", String.class))
                .build();
    }

    private String formatTime(Object sqlTime) {
        if (sqlTime == null) return null;
        try {
            LocalTime t = ((java.sql.Time) sqlTime).toLocalTime();
            return t.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return sqlTime.toString();
        }
    }

    public List<ColumnMeta> getColumns() {
        return columns;
    }
}