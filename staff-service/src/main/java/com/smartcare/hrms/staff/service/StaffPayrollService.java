package com.smartcare.hrms.staff.service;

import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import com.smartcare.hrms.common.entity.payroll.salary.EmpSalaryComponentDetails;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.payroll.salary.EmpMonthlyPayrollSummaryRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffPayrollSlipDTO;
import com.smartcare.hrms.staff.dto.StaffSalaryComponentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StaffPayrollService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmpMonthlyPayrollSummaryRepository payrollSummaryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MessageResource messageResource;

    /**
     * Get real monthly payslips list for staff.
     */
    public List<StaffPayrollSlipDTO> getMySalarySlips(String empId) {
        if (ValidateUtils.isStringEmpty(empId)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        Employee employee = employeeRepository.findById(UUID.fromString(empId))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        String sql = "SELECT emps.id AS id, asl.month_year AS monthYear, " +
                "emps.payable_days AS payableDays, emps.lop_days AS lopDays, " +
                "emps.ttl_payable_earnings AS totalEarnings, " +
                "(COALESCE(emps.ttl_payable_std_deductions,0) + COALESCE(emps.ttl_payable_othr_deductions,0)) AS totalDeductions, " +
                "emps.net_payable AS netPayable " +
                "FROM emp_monthly_payroll_summary emps " +
                "INNER JOIN attendance_salary_link asl ON asl.id = emps.attendance_salary_link_id " +
                "WHERE asl.employee_id = :empId " +
                "ORDER BY asl.month_year DESC";

        Query query = entityManager.createNativeQuery(sql, jakarta.persistence.Tuple.class);
        query.setParameter("empId", empId);
        List<jakarta.persistence.Tuple> results = query.getResultList();

        if (CollectionUtils.isEmpty(results)) {
            return Collections.emptyList();
        }

        List<StaffPayrollSlipDTO> slips = new ArrayList<>();
        for (jakarta.persistence.Tuple row : results) {
            String monthYear = row.get("monthYear", String.class);
            String[] parts = monthYear != null ? monthYear.split("-") : new String[]{"", ""};
            String year = parts.length > 0 ? parts[0] : "";
            String month = parts.length > 1 ? parts[1] : "";

            slips.add(StaffPayrollSlipDTO.builder()
                    .month(month)
                    .year(year)
                    .monthYearDisplay(monthYear)
                    .payableDays(row.get("payableDays", Double.class))
                    .lopDays(row.get("lopDays", Double.class))
                    .grossSalary(getDoubleValue(row.get("totalEarnings")))
                    .totalDeductions(getDoubleValue(row.get("totalDeductions")))
                    .netSalary(getDoubleValue(row.get("netPayable")))
                    .build());
        }

        return slips;
    }

    /**
     * Get detailed component breakdown for a specific monthly salary slip.
     */
    public StaffPayrollSlipDTO getPayslipDetails(String empId, String monthYear) {
        if (ValidateUtils.isStringEmpty(empId) || ValidateUtils.isStringEmpty(monthYear)) {
            throw new ValidationException("Employee ID and Month-Year are required");
        }

        String sql = "SELECT emps.id FROM emp_monthly_payroll_summary emps " +
                "INNER JOIN attendance_salary_link asl ON asl.id = emps.attendance_salary_link_id " +
                "WHERE asl.employee_id = :empId AND asl.month_year = :monthYear";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("empId", empId);
        query.setParameter("monthYear", monthYear);
        List<?> result = query.getResultList();

        if (CollectionUtils.isEmpty(result)) {
            throw new ValidationException("Salary slip not found for " + monthYear);
        }

        Object rowId = result.get(0);
        UUID payrollId = (rowId instanceof byte[]) ? UUID.nameUUIDFromBytes((byte[]) rowId) : UUID.fromString(rowId.toString());

        Optional<EmpMonthlyPayrollSummary> summaryOpt = payrollSummaryRepository.findById(payrollId);
        if (summaryOpt.isEmpty()) {
            throw new ValidationException("Salary details not found");
        }

        EmpMonthlyPayrollSummary summary = summaryOpt.get();
        List<StaffSalaryComponentDTO> components = new ArrayList<>();

        if (!CollectionUtils.isEmpty(summary.getSalaryComponents())) {
            for (EmpSalaryComponentDetails comp : summary.getSalaryComponents()) {
                components.add(StaffSalaryComponentDTO.builder()
                        .componentName(comp.getComponentName())
                        .componentCode(comp.getComponentCode())
                        .componentType(comp.getComponentType() != null ? comp.getComponentType().name() : "EARNING")
                        .amount(comp.getPayableAmount() != null ? comp.getPayableAmount().doubleValue() : 0.0)
                        .build());
            }
        }

        String[] parts = monthYear.split("-");
        return StaffPayrollSlipDTO.builder()
                .month(parts.length > 1 ? parts[1] : "")
                .year(parts.length > 0 ? parts[0] : "")
                .monthYearDisplay(monthYear)
                .payableDays(summary.getPayableDays())
                .lopDays(summary.getLossOfPayDays())
                .grossSalary(summary.getTotalPayableEarnings() != null ? summary.getTotalPayableEarnings().doubleValue() : 0.0)
                .totalDeductions(summary.getTotalPayableStandardDeductions() != null ? summary.getTotalPayableStandardDeductions().doubleValue() : 0.0)
                .netSalary(summary.getNetPayable() != null ? summary.getNetPayable().doubleValue() : 0.0)
                .components(components)
                .build();
    }

    private Double getDoubleValue(Object val) {
        if (val == null) return 0.0;
        if (val instanceof BigDecimal) return ((BigDecimal) val).doubleValue();
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }
}
