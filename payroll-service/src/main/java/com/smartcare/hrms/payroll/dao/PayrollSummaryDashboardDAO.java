package com.smartcare.hrms.payroll.dao;

import com.smartcare.hrms.common.basedao.BaseDashboardDAO;
import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import com.smartcare.hrms.common.enumeration.PayrollStatus;
import com.smartcare.hrms.common.pagination.ColumnMeta;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.pagination.PaginationInfo;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.common.dto.payroll.salary.MonthlySalarySummaryDashboardDTO;
import com.smartcare.hrms.common.dto.payroll.salary.PayrollSummaryRequestDTO;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.smartcare.hrms.common.utility.HelperUtil.toBoolean;

@Slf4j
@Repository
public class PayrollSummaryDashboardDAO extends BaseDashboardDAO {
    private final static String baseCountQuery = "SELECT COUNT(*) FROM emp_dtls ed "
            + "INNER JOIN company_details c ON c.id = ed.company_id "
            + "INNER JOIN branch_details b ON b.id = ed.branch_id "
            + "INNER JOIN department dp ON dp.id = ed.department_id "
            + "INNER JOIN designation dg ON dg.id = ed.designation_id "
            + "INNER JOIN category ct ON ct.id = ed.category_id "
            + "INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id "
            + "INNER JOIN grades g ON g.id = ed.grade_id ";

    private final static String selfCountQuery = "SELECT COUNT(*) FROM emp_dtls ed WHERE ed.id = :empId ";

    private final String GET_PAYROLL_SUMMARY_DASHBOARD_QUERY = "SELECT emps.id AS payrollId,asl.id AS linkId,asl.night_shift_exist nightShiftExist,\n " +
            "ed.id AS empId,ed.formal_name AS empName,ed.emp_code AS empCode,ed.gender AS empGender, \n" +
            "emps.payable_days AS payableDays,emps.lop_days AS lopDays,\n" +
            "emps.ttl_earnings AS totalEarnings,emps.ttl_std_deductions AS totalStdDeductions,emps.ttl_othr_deductions AS totalOtherDeduction,\n" +
            "emps.ttl_payable_earnings AS totalPayableEarnings,emps.ttl_payable_std_deductions AS totalPayableStdDeductions,emps.ttl_payable_othr_deductions AS totalPayableOtherDeductions,emps.net_payable AS netPayable,\n" +
            "esi.payroll_state AS payrollState, esi.pf_applicable AS pfEnabled ,esi.esic_applicable AS esicEnabled,esi.pt_applicable AS ptEnabled, \n"+
            "asl.nsa_amt AS nsaAmount,asl.a_status AS attendanceStatus,asl.p_status AS payrollStatus\n" +
            "FROM emp_dtls ed\n " +
            "INNER JOIN attendance_salary_link asl ON asl.employee_id = ed.id  \n" +
            "LEFT JOIN employee_statutory_info esi ON esi.emp_id = ed.id  \n" +
            "LEFT JOIN emp_monthly_payroll_summary emps ON emps.attendance_salary_link_id = asl.id";


    private static List<ColumnMeta> columns = new ArrayList<>(List.of(new ColumnMeta[]{
            new ColumnMeta("Name", "ed.formal_name"),
            new ColumnMeta("Code", "ed.emp_code"),
            new ColumnMeta("Gender", "ed.gender"),

            new ColumnMeta("Month", "NA"),
            new ColumnMeta("Payable", "emps.payable_days"),
            new ColumnMeta("Lop", "emps.lop_days"),

            new ColumnMeta("Gross", "NA"),
            new ColumnMeta("Std Deds", "NA"),
            new ColumnMeta("Other Deds", "NA"),

            new ColumnMeta("Gross", "NA"),
            new ColumnMeta("Std deds", "NA"),
            new ColumnMeta("Other Deds", "NA"),
            new ColumnMeta("LOP", "NA"),
            new ColumnMeta("Net payable", "NA"),
            new ColumnMeta("Attendance", "NA"),
            new ColumnMeta("Payroll", "NA")
    }));

    private PaginationInfo getPaginationInfo(PayrollSummaryRequestDTO searchDTO) {

        boolean isAdmin="Admin".equalsIgnoreCase(searchDTO.getJobTitle());

        Query countQuery;
        if(isAdmin){
            countQuery= entityManager.createNativeQuery(baseCountQuery);
        }else{
            countQuery=entityManager.createNativeQuery(selfCountQuery);
            countQuery.setParameter("empId",searchDTO.getEmpId());

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

    private String constructWhereClause(PayrollSummaryRequestDTO searchDTO) {
        StringBuilder whereClause = new StringBuilder(" WHERE asl.month_year = :monthYear ");

        boolean isAdmin="Admin".equalsIgnoreCase(searchDTO.getJobTitle());
        if(!isAdmin){
            whereClause.append(" AND ed.id=:empId ");
            return whereClause.toString();
        }
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
        return whereClause.toString();
    }

    // Helper method to set parameters for both queries
    private void setQueryParameters(Query query, PayrollSummaryRequestDTO dto) {
        boolean isAdmin="Admin".equalsIgnoreCase(dto.getJobTitle());
        query.setParameter("monthYear", dto.getMonthYear().trim());

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

    public PaginatedDataResponse<MonthlySalarySummaryDashboardDTO> getPayrollSummaryDashboard(PayrollSummaryRequestDTO searchDTO) {
        // Use Locale.ENGLISH to ensure months like MARCH are recognized correctly
        // Parse to YearMonth
        YearMonth yearMonth = YearMonth.parse(searchDTO.getMonthYear(), DateTimeFormatter.ofPattern("yyyy-MM", Locale.ENGLISH));

        // Get number of days
        Double daysInMonth = (double) yearMonth.lengthOfMonth();

        PaginationInfo paginationInfo = getPaginationInfo(searchDTO);
        StringBuilder sqlQuery = new StringBuilder(GET_PAYROLL_SUMMARY_DASHBOARD_QUERY);
        sqlQuery.append(constructWhereClause(searchDTO));
        sqlQuery.append(paginationUtils.getSortingQuery(columns, paginationInfo));
        sqlQuery.append(paginationUtils.getLimitSqlString(paginationInfo));
        // EXECUTE DATA QUERY
        log.debug("Executing SQL query: {}", sqlQuery);
        log.debug("SQL query Filters: {}", searchDTO);
        Query dataQuery = entityManager.createNativeQuery(sqlQuery.toString(), Tuple.class);
        setQueryParameters(dataQuery, searchDTO);

        List<Tuple> results = dataQuery.getResultList();
        log.debug(sqlQuery.toString());
        List<Object> dataList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(results)) {
            for (Tuple result : results) {
                MonthlySalarySummaryDashboardDTO dashboardDTO = new MonthlySalarySummaryDashboardDTO();
                dashboardDTO.setMonthPayrollId(result.get("payrollId", String.class));
                MonthlySalarySummaryDashboardDTO.EmployeeDetails employeeDetails = MonthlySalarySummaryDashboardDTO.EmployeeDetails.builder()
                        .employeeId(result.get("empId", String.class))
                        .employeeName(result.get("empName", String.class))
                        .employeeCode(result.get("empCode", String.class))
                        .payrollState(result.get("payrollState", String.class))
                        .gender(result.get("empGender", String.class))
                        .build();
                dashboardDTO.setEmployeeDetails(employeeDetails);
                MonthlySalarySummaryDashboardDTO.DaysDetails daysDetails = MonthlySalarySummaryDashboardDTO.DaysDetails.builder()
                        .totalDaysInMonth(daysInMonth)
                        .payableDays(result.get("payableDays", Double.class))
                        .lossOfPayDays(result.get("lopDays", Double.class))
                        .build();
                dashboardDTO.setDaysDetails(daysDetails);
                MonthlySalarySummaryDashboardDTO.StatutoryFlag statutoryFlag = MonthlySalarySummaryDashboardDTO.StatutoryFlag.builder()
                        .state(result.get("payrollState", String.class))
                        .pfEnabled(toBoolean(result.get("pfEnabled")))
                        .esicEnabled(toBoolean(result.get("esicEnabled")))
                        .ptEnabled(toBoolean(result.get("ptEnabled")))
                        .build();
                dashboardDTO.setStatutoryFlags(statutoryFlag);
                dashboardDTO.setNightShiftExist(toBoolean(result.get("nightShiftExist")));
                MonthlySalarySummaryDashboardDTO.ProcessStatus processStatus=null;
                if(result.get("linkId", String.class) != null) {
                    processStatus = MonthlySalarySummaryDashboardDTO.ProcessStatus.builder()
                            .attendanceStatus(AttendanceStatusEnum.valueOf(result.get("attendanceStatus", String.class)))
                            .payrollStatus(PayrollStatus.valueOf(result.get("payrollStatus", String.class)))
                            .build();
                    dashboardDTO.setStatus(processStatus);
                    BigDecimal nsaAmount = BigDecimal.valueOf(
                            result.get("nsaAmount") == null ? 0.0 :
                                    (result.get("nsaAmount") instanceof Number
                                            ? ((Number) result.get("nsaAmount")).doubleValue()
                                            : Double.parseDouble(result.get("nsaAmount").toString())));
                    dashboardDTO.setNsaAmount(nsaAmount);

                }else{
                    processStatus = MonthlySalarySummaryDashboardDTO.ProcessStatus.builder()
                            .attendanceStatus(AttendanceStatusEnum.NA)
                            .build();
                    dashboardDTO.setStatus(processStatus);
                }

                if(processStatus.getAttendanceStatus().equals(AttendanceStatusEnum.LOCKED) || processStatus.getAttendanceStatus().equals(AttendanceStatusEnum.FROZEN)) {
                    if (!processStatus.getPayrollStatus().equals(PayrollStatus.PENDING)) {
                        MonthlySalarySummaryDashboardDTO.CurrentMonthPayable currentMonthPayable =
                                MonthlySalarySummaryDashboardDTO.CurrentMonthPayable.builder()
                                        .totalEarnings(BigDecimal.valueOf(
                                                result.get("totalPayableEarnings") == null ? 0.0 :
                                                        (result.get("totalPayableEarnings") instanceof Number
                                                                ? ((Number) result.get("totalPayableEarnings")).doubleValue()
                                                                : Double.parseDouble(result.get("totalPayableEarnings").toString()))
                                        ))
                                        .totalStdDeductions(BigDecimal.valueOf(
                                                result.get("totalPayableStdDeductions") == null ? 0.0 :
                                                        (result.get("totalPayableStdDeductions") instanceof Number
                                                                ? ((Number) result.get("totalPayableStdDeductions")).doubleValue()
                                                                : Double.parseDouble(result.get("totalPayableStdDeductions").toString()))
                                        ))
                                        .totalOtherDeductions(BigDecimal.valueOf(
                                               result.get("totalPayableOtherDeductions") == null ? 0.0 :
                                                  (result.get("totalPayableOtherDeductions") instanceof Number
                                                     ? ((Number) result.get("totalPayableOtherDeductions")).doubleValue()
                                                        : Double.parseDouble(result.get("totalPayableOtherDeductions").toString()))
                                          ))
                                        .netPayable(BigDecimal.valueOf(
                                                result.get("netPayable") == null ? 0.0 :
                                                        (result.get("netPayable") instanceof Number
                                                                ? ((Number) result.get("netPayable")).doubleValue()
                                                                : Double.parseDouble(result.get("netPayable").toString()))
                                        ))
                                        .build();
                        MonthlySalarySummaryDashboardDTO.MonthlyGross monthlyGross =
                                MonthlySalarySummaryDashboardDTO.MonthlyGross.builder()
                                        .totalEarnings(BigDecimal.valueOf(
                                                result.get("totalPayableEarnings") == null ? 0.0 :
                                                        (result.get("totalPayableEarnings") instanceof Number
                                                                ? ((Number) result.get("totalPayableEarnings")).doubleValue()
                                                                : Double.parseDouble(result.get("totalPayableEarnings").toString()))
                                        ))
                                        .totalStdDeductions(BigDecimal.valueOf(
                                                result.get("totalPayableStdDeductions") == null ? 0.0 :
                                                        (result.get("totalPayableStdDeductions") instanceof Number
                                                                ? ((Number) result.get("totalPayableStdDeductions")).doubleValue()
                                                                : Double.parseDouble(result.get("totalPayableStdDeductions").toString()))
                                        ))
                                        .totalOtherDeductions(BigDecimal.valueOf(
                                                result.get("totalPayableOtherDeductions") == null ? 0.0 :
                                                        (result.get("totalPayableOtherDeductions") instanceof Number
                                                                ? ((Number) result.get("totalPayableOtherDeductions")).doubleValue()
                                                                : Double.parseDouble(result.get("totalPayableOtherDeductions").toString()))
                                        ))
                                        .build();
                        dashboardDTO.setMonthlyGross(monthlyGross);
                        dashboardDTO.setCurrentMonthPayable(currentMonthPayable);
                    }
                }
                dataList.add(dashboardDTO);
            }
        }
        return (PaginatedDataResponse<MonthlySalarySummaryDashboardDTO>) paginationUtils.getPaginatedResponse(columns, dataList, searchDTO.getPaginationInfo());
    }
}
