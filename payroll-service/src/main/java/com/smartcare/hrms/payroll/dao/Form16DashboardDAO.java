package com.smartcare.hrms.payroll.dao;

import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.payroll.dto.form16.Form16DashboardRequestDTO;
import com.smartcare.hrms.payroll.dto.form16.Form16DTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class Form16DashboardDAO {

    private @Autowired EntityManager entityManager;
    private @Autowired EmployeeRepository employeeRepository;

    public List<Form16DTO> getForm16EmployeeData(Form16DashboardRequestDTO requestDTO) {

        String[] financialYear = getFinancialYear(requestDTO.getFinancialYear());

        TypedQuery<EmpMonthlyPayrollSummary> query = entityManager.createQuery(
                "SELECT eps " +
                        "FROM EmpMonthlyPayrollSummary eps " +
                        "JOIN FETCH eps.attendanceSalaryLink asl " +
                        "WHERE asl.monthYear BETWEEN :startMonth AND :endMonth " +
                        "AND UPPER(asl.payrollStatus) = 'PUBLISHED' " +
                        "ORDER BY asl.employeeId, asl.monthYear",
                EmpMonthlyPayrollSummary.class);

        query.setParameter("startMonth", financialYear[0]);
        query.setParameter("endMonth", financialYear[1]);

        List<EmpMonthlyPayrollSummary> payrollRecords = query.getResultList();

        if (CollectionUtils.isEmpty(payrollRecords)) {
            return new ArrayList<>();
        }

        /*
         * IMPORTANT:
         * Group all payroll records by employee.
         * This makes sure one employee appears only ONCE.
         */
        Map<String, List<EmpMonthlyPayrollSummary>> employeePayrollMap =
                payrollRecords.stream()
                        .filter(record -> record.getAttendanceSalaryLink() != null)
                        .collect(Collectors.groupingBy(
                                record -> record.getAttendanceSalaryLink().getEmployeeId()
                        ));

        /*
         * Get employee information.
         */
        List<Employee> employees = employeeRepository.findAllById(
                employeePayrollMap.keySet().stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList()));

        Map<String, Employee> employeeMap = employees.stream()
                .collect(Collectors.toMap(
                        employee -> employee.getId().toString(),
                        employee -> employee,
                        (existing, replacement) -> existing));

        List<Form16DTO> response = new ArrayList<>();

        /*
         * ONE DTO for ONE employee.
         */
        for (Map.Entry<String, List<EmpMonthlyPayrollSummary>> entry :
                employeePayrollMap.entrySet()) {

            Employee employee = employeeMap.get(entry.getKey());

            if (employee == null) {
                continue;
            }

            /*
             * Apply filters.
             */
            if (!matchesEmployee(employee, requestDTO)) {
                continue;
            }

            Form16DTO dto = buildForm16DTO(
                    employee,
                    entry.getValue(),
                    requestDTO.getFinancialYear());

            response.add(dto);
        }

        /*
         * Sort employees alphabetically.
         */
        response.sort(Comparator.comparing(
                Form16DTO::getEmployeeCode,
                Comparator.nullsLast(String::compareToIgnoreCase)));

        return response;
    }

    public Form16DTO getForm16EmployeeDetails(
            String employeeId,
            String financialYear) {

        Employee employee = employeeRepository.findById(UUID.fromString(employeeId))
                .orElseThrow(() ->
                        new RuntimeException("Employee not found: " + employeeId));

        String[] year = getFinancialYear(financialYear);

        TypedQuery<EmpMonthlyPayrollSummary> query = entityManager.createQuery(
                "SELECT eps " +
                        "FROM EmpMonthlyPayrollSummary eps " +
                        "JOIN FETCH eps.attendanceSalaryLink asl " +
                        "WHERE asl.employeeId = :employeeId " +
                        "AND asl.monthYear BETWEEN :startMonth AND :endMonth " +
                        "AND UPPER(asl.payrollStatus) = 'PUBLISHED' " +
                        "ORDER BY asl.monthYear",
                EmpMonthlyPayrollSummary.class);

        query.setParameter("employeeId", employeeId);
        query.setParameter("startMonth", year[0]);
        query.setParameter("endMonth", year[1]);

        List<EmpMonthlyPayrollSummary> records = query.getResultList();

        if (CollectionUtils.isEmpty(records)) {
            throw new RuntimeException(
                    "Payroll data not found for employee: " + employeeId);
        }

        return buildForm16DTO(employee, records, financialYear);
    }

    private Form16DTO buildForm16DTO(
            Employee employee,
            List<EmpMonthlyPayrollSummary> records,
            String financialYear) {

        Double totalEarnings = 0.0;
        Double totalStandardDeductions = 0.0;
        Double totalOtherDeductions = 0.0;
        Double netPayable = 0.0;

        String payrollStatus = "PENDING";

        /*
         * Calculate payroll values from database records.
         */
        for (EmpMonthlyPayrollSummary record : records) {

            if (record.getTotalEarnings() != null) {    
                totalEarnings += record.getTotalEarnings().doubleValue();
            }

            if (record.getTotalStandardDeductions() != null) {
                totalStandardDeductions += record.getTotalStandardDeductions().doubleValue();
            }

            if (record.getTotalOtherDeductions() != null) {
                totalOtherDeductions += record.getTotalOtherDeductions().doubleValue();
            }

            if (record.getNetPayable() != null) {
                netPayable += record.getNetPayable().doubleValue();
            }

            /*
             * Published should win if any payroll record
             * for the employee is Published.
             */
            if (record.getAttendanceSalaryLink() != null
                    && record.getAttendanceSalaryLink().getPayrollStatus() != null) {

                String status = record.getAttendanceSalaryLink()
                        .getPayrollStatus().getValue();

                if (status != null) {

                    if ("Published".equalsIgnoreCase(status)) {
                        payrollStatus = "Published";

                    } else if ("Draft".equalsIgnoreCase(status)
                            && !"Published".equalsIgnoreCase(payrollStatus)) {

                        payrollStatus = "Draft";

                    } else if (!"Published".equalsIgnoreCase(payrollStatus)
                            && !"Draft".equalsIgnoreCase(payrollStatus)) {

                        payrollStatus = status;
                    }
                }
            }
        }

        Double totalDeductions =
                totalStandardDeductions + totalOtherDeductions;

        Form16DTO dto = new Form16DTO();

        /*
         * Employee information
         */
        dto.setEmployeeId(employee.getId().toString());
        dto.setEmployeeCode(employee.getEmpCode());
        dto.setEmployeeName(employee.getFormalName());

        /*
         * PAN
         */
        dto.setPan(employee.getPanNumber());

        /*
         * Designation
         */
        dto.setDesignation(employee.getDesignation() != null
                ? employee.getDesignation().getName()
                : null);

        /*
         * Department
         */
        dto.setDepartment(employee.getDepartment() != null
                ? employee.getDepartment().getName()
                : null);

        /*
         * Financial Year
         */
        dto.setFinancialYear(financialYear);

        /*
         * Form 16 Number
         */
        dto.setForm16Number(
                "F16/" + financialYear + "/" + employee.getEmpCode());

        /*
         * Payroll values
         */
        dto.setTotalEarnings(totalEarnings);
        dto.setTotalStandardDeductions(totalStandardDeductions);
        dto.setTotalOtherDeductions(totalOtherDeductions);
        dto.setTotalDeductions(totalDeductions);
        dto.setNetPayable(netPayable);

        /*
         * Payroll status
         */

        dto.setPayrollStatus(payrollStatus);
        /*
         * Form 16 generated
         */
        dto.setForm16Generated("Published".equalsIgnoreCase(payrollStatus));
        /*
         * Download status.
         *
         * For now this is false because there is
         * no download-status database field being
         * used here.
         */
        dto.setForm16Downloaded(false);

        /*
         * These values are currently zero because
         * their exact EmpMonthlyPayrollSummary fields
         * have not been identified yet.
         */
        dto.setBasicPay(0.0);
        dto.setHra(0.0);
        dto.setSpecialAllowance(0.0);
        dto.setProfessionalTax(0.0);
        dto.setIncomeTax(0.0);

        return dto;
    }

    private boolean matchesEmployee(
            Employee employee,
            Form16DashboardRequestDTO requestDTO) {

        if (requestDTO.getEmployeeName() != null
                && !requestDTO.getEmployeeName().trim().isEmpty()) {

            if (employee.getFormalName() == null
                    || !employee.getFormalName().toLowerCase()
                    .contains(requestDTO.getEmployeeName().trim().toLowerCase())) {

                return false;
            }
        }

        if (requestDTO.getEmployeeCode() != null
                && !requestDTO.getEmployeeCode().trim().isEmpty()) {

            if (employee.getEmpCode() == null
                    || !employee.getEmpCode().equalsIgnoreCase(
                    requestDTO.getEmployeeCode().trim())) {

                return false;
            }
        }

        if (requestDTO.getDepartment() != null
                && !requestDTO.getDepartment().trim().isEmpty()) {

            if (employee.getDepartment() == null
                    || employee.getDepartment().getName() == null
                    || !employee.getDepartment().getName().equalsIgnoreCase(
                    requestDTO.getDepartment().trim())) {

                return false;
            }
        }

        return true;
    }

    private String[] getFinancialYear(String financialYear) {

        if (financialYear == null
                || !financialYear.matches("\\d{4}-\\d{2}")) {

            throw new RuntimeException(
                    "Financial year must be in format YYYY-YY");
        }

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        return new String[]{
                startYear + "-04",
                endYear + "-03"
        };
    }
}