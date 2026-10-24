package com.smartcare.hrms.hiring.service;

import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.hiring.converter.EsicChallanConverter;
import com.smartcare.hrms.hiring.converter.EsicEmployeeDetailsConverter;
import com.smartcare.hrms.hiring.dao.EsicChallanDAO;
import com.smartcare.hrms.hiring.dto.*;
import com.smartcare.hrms.hiring.model.entity.EsicChallan;
import com.smartcare.hrms.hiring.model.entity.EsicChallanEmployee;
import com.smartcare.hrms.hiring.repository.EsicChallanEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EsicChallanService {

    @Value("${esic.wage.ceiling}")
    private BigDecimal esicWageCeiling;

    private @Autowired
    EsicChallanDAO challanDAO;

    private @Autowired
    EmployeeRepository employeeRepository;

    private @Autowired
    EsicChallanConverter challanConverter;

    private @Autowired
    EsicEmployeeDetailsConverter employeeDetailsConverter;

    private @Autowired
    EsicChallanEmployeeRepository challanEmployeeRepository;

    private static final BigDecimal EMPLOYEE_RATE =
            new BigDecimal("0.0075");

    private static final BigDecimal EMPLOYER_RATE =
            new BigDecimal("0.0325");

    public PageResponse<EsicChallanListDTO> getChallanList(
            EsicChallanRequestDTO requestDTO) {

        List<EsicChallan> challans =
                challanDAO.getGeneratedChallans(
                        requestDTO.getFinancialYear(),
                        requestDTO.getMonth()
                );

        List<EsicChallanListDTO> response =
                new ArrayList<>();

        for (EsicChallan challan :
                challans) {

            EsicChallanListDTO dto =
                    challanConverter.convertToDTO(
                            challan.getChallanNumber(),
                            challan.getMonth(),
                            challan.getTotalEmployees(),
                            challan.getEmployeeShare(),
                            challan.getEmployerShare(),
                            challan.getTotalAmount(),
                            challan.getStatus()
                    );

            dto.setGeneratedOn(
                    challan.getGeneratedOn()
            );

            response.add(dto);
        }

        return createPageResponse(
                response,
                requestDTO
        );
    }

    @Transactional
    public EsicChallanListDTO generateChallan(
            EsicChallanRequestDTO requestDTO) {

        validateGenerateRequest(
                requestDTO
        );

        String monthYear =
                challanDAO.getResolvedMonthYear(
                        requestDTO.getMonth(),
                        requestDTO.getFinancialYear()
                );

        EsicChallan existing =
                challanDAO.getExistingChallan(
                        requestDTO.getFinancialYear(),
                        monthYear
                );

        if (existing != null) {

            throw new RuntimeException(
                    "ESIC challan already generated for "
                            + requestDTO.getMonth()
                            + " "
                            + requestDTO.getFinancialYear()
            );
        }

        List<EmpMonthlyPayrollSummary> payrollRecords =
                challanDAO.getPayrollRecordsForGeneration(
                        requestDTO.getFinancialYear(),
                        requestDTO.getMonth()
                );

        if (payrollRecords == null
                || payrollRecords.isEmpty()) {

            throw new RuntimeException(
                    "Published payroll data not found for "
                            + requestDTO.getMonth()
                            + " "
                            + requestDTO.getFinancialYear()
            );
        }

        Set<String> employeeIds =
                payrollRecords.stream()
                        .filter(record ->
                                record.getAttendanceSalaryLink() != null)
                        .map(record ->
                                record.getAttendanceSalaryLink()
                                        .getEmployeeId())
                        .filter(Objects::nonNull)
                        .filter(id -> !id.trim().isEmpty())
                        .collect(Collectors.toSet());

        List<Employee> employees =
                employeeRepository.findAllById(
                        employeeIds.stream()
                                .map(UUID::fromString)
                                .collect(Collectors.toList())
                );

        Map<String, Employee> employeeMap =
                employees.stream()
                        .collect(Collectors.toMap(
                                employee ->
                                        employee.getId().toString(),
                                employee ->
                                        employee,
                                (existingEmployee, replacement) ->
                                        existingEmployee
                        ));

        BigDecimal totalEmployeeShare =
                BigDecimal.ZERO;

        BigDecimal totalEmployerShare =
                BigDecimal.ZERO;

        int totalEmployees =
                0;

        Set<String> processedEmployees =
                new HashSet<>();

        List<EsicChallanEmployee> employeeDetails =
                new ArrayList<>();

        for (EmpMonthlyPayrollSummary record :
                payrollRecords) {

            if (record.getAttendanceSalaryLink() == null) {
                continue;
            }

            String employeeId =
                    record.getAttendanceSalaryLink()
                            .getEmployeeId();

            if (employeeId == null) {
                continue;
            }

            if (!processedEmployees.add(employeeId)) {
                continue;
            }

            Employee employee =
                    employeeMap.get(employeeId);

            if (employee == null) {
                continue;
            }

            BigDecimal wages =
                    record.getTotalEarnings();

            if (wages == null
                    || wages.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal employeeShare =
                    wages
                            .multiply(EMPLOYEE_RATE)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal employerShare =
                    wages
                            .multiply(EMPLOYER_RATE)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal total =
                    employeeShare
                            .add(employerShare)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            totalEmployeeShare =
                    totalEmployeeShare.add(
                            employeeShare
                    );

            totalEmployerShare =
                    totalEmployerShare.add(
                            employerShare
                    );

            totalEmployees++;

            String department =
                    employee.getDepartment() != null
                            ? employee.getDepartment().getName()
                            : null;

            EsicChallanEmployee detail =
                    new EsicChallanEmployee();

            detail.setEmployeeCode(
                    employee.getEmpCode()
            );

            detail.setEmployeeName(
                    employee.getFormalName()
            );

            detail.setEsicNumber(
                    generateMockEsicNumber(
                            employee
                    )
            );

            detail.setDepartment(
                    department
            );

            detail.setWages(
                    wages.setScale(
                            2,
                            RoundingMode.HALF_UP
                    )
            );

            detail.setEmployeeShare(
                    employeeShare
            );

            detail.setEmployerShare(
                    employerShare
            );

            detail.setTotal(
                    total
            );

            detail.setStatus(
                    "Active"
            );

            employeeDetails.add(
                    detail
            );
        }

        if (totalEmployees == 0) {

            throw new RuntimeException(
                    "No valid employee payroll data found for "
                            + requestDTO.getMonth()
            );
        }

        totalEmployeeShare =
                totalEmployeeShare.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        totalEmployerShare =
                totalEmployerShare.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal totalAmount =
                totalEmployeeShare
                        .add(totalEmployerShare)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        int nextSequence =
                getNextSequence();

        String challanNumber =
                String.format(
                        "ESIC/%s/%03d",
                        requestDTO.getFinancialYear(),
                        nextSequence
                );

        EsicChallan challan =
                new EsicChallan();

        challan.setChallanNumber(
                challanNumber
        );

        challan.setFinancialYear(
                requestDTO.getFinancialYear()
        );

        challan.setMonth(
                getMonthName(monthYear)
        );

        challan.setMonthYear(
                monthYear
        );

        challan.setTotalEmployees(
                totalEmployees
        );

        challan.setEmployeeShare(
                totalEmployeeShare
        );

        challan.setEmployerShare(
                totalEmployerShare
        );

        challan.setTotalAmount(
                totalAmount
        );

        challan.setStatus(
                "PENDING"
        );

        challan.setGeneratedOn(
                java.time.LocalDate.now()
        );

        EsicChallan savedChallan =
                challanDAO.saveChallan(
                        challan
                );

        for (EsicChallanEmployee detail :
                employeeDetails) {

            detail.setChallanId(
                    savedChallan.getId()
            );
        }

        challanEmployeeRepository.saveAll(
                employeeDetails
        );

        EsicChallanListDTO response =
                challanConverter.convertToDTO(
                        savedChallan.getChallanNumber(),
                        savedChallan.getMonth(),
                        savedChallan.getTotalEmployees(),
                        savedChallan.getEmployeeShare(),
                        savedChallan.getEmployerShare(),
                        savedChallan.getTotalAmount(),
                        savedChallan.getStatus()
                );

        response.setGeneratedOn(
                savedChallan.getGeneratedOn()
        );

        return response;
    }

    public EsicEmployeeDetailsResponseDTO getEmployeeDetails(
            EsicChallanRequestDTO requestDTO) {

        List<EmpMonthlyPayrollSummary> payrollRecords =
                challanDAO.getEmployeePayrollData(
                        requestDTO.getFinancialYear(),
                        requestDTO.getMonth()
                );

        if (payrollRecords == null
                || payrollRecords.isEmpty()) {

            EsicEmployeeSummaryDTO summary =
                    new EsicEmployeeSummaryDTO(
                            0,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            0,
                            0
                    );

            PageResponse<EsicEmployeeDetailsDTO> emptyPage =
                    createEmployeePageResponse(
                            new ArrayList<>(),
                            requestDTO
                    );

            return new EsicEmployeeDetailsResponseDTO(
                    summary,
                    emptyPage
            );
        }

        Set<String> employeeIds =
                payrollRecords.stream()
                        .filter(record ->
                                record.getAttendanceSalaryLink() != null)
                        .map(record ->
                                record.getAttendanceSalaryLink()
                                        .getEmployeeId())
                        .filter(Objects::nonNull)
                        .filter(id -> !id.trim().isEmpty())
                        .collect(Collectors.toSet());

        List<Employee> employees =
                employeeRepository.findAllById(
                        employeeIds.stream()
                                .map(UUID::fromString)
                                .collect(Collectors.toList())
                );

        Map<String, Employee> employeeMap =
                employees.stream()
                        .collect(Collectors.toMap(
                                employee ->
                                        employee.getId().toString(),
                                employee ->
                                        employee,
                                (existing, replacement) ->
                                        existing
                        ));

        BigDecimal totalEmployeeShare =
                BigDecimal.ZERO;

        BigDecimal totalEmployerShare =
                BigDecimal.ZERO;

        int totalEmployees = 0;

        int belowCeiling = 0;

        int aboveCeiling = 0;

        Set<String> summaryEmployees =
                new HashSet<>();

        for (EmpMonthlyPayrollSummary record :
                payrollRecords) {

            if (record.getAttendanceSalaryLink() == null) {
                continue;
            }

            String employeeId =
                    record.getAttendanceSalaryLink()
                            .getEmployeeId();

            if (employeeId == null) {
                continue;
            }

            if (!summaryEmployees.add(employeeId)) {
                continue;
            }

            Employee employee =
                    employeeMap.get(employeeId);

            if (employee == null) {
                continue;
            }

            BigDecimal wages =
                    record.getTotalEarnings();

            if (wages == null
                    || wages.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal employeeShare =
                    wages
                            .multiply(EMPLOYEE_RATE)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal employerShare =
                    wages
                            .multiply(EMPLOYER_RATE)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            totalEmployees++;

            totalEmployeeShare =
                    totalEmployeeShare.add(
                            employeeShare
                    );

            totalEmployerShare =
                    totalEmployerShare.add(
                            employerShare
                    );

            if (isBelowCeiling(wages)) {
                belowCeiling++;
            } else {
                aboveCeiling++;
            }
        }

        BigDecimal totalContribution =
                totalEmployeeShare
                        .add(totalEmployerShare)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        EsicEmployeeSummaryDTO summary =
                new EsicEmployeeSummaryDTO(
                        totalEmployees,
                        totalEmployeeShare,
                        totalEmployerShare,
                        totalContribution,
                        belowCeiling,
                        aboveCeiling
                );

        List<EsicEmployeeDetailsDTO> response =
                new ArrayList<>();

        Set<String> processedEmployees =
                new HashSet<>();

        for (EmpMonthlyPayrollSummary record :
                payrollRecords) {

            if (record.getAttendanceSalaryLink() == null) {
                continue;
            }

            String employeeId =
                    record.getAttendanceSalaryLink()
                            .getEmployeeId();

            if (employeeId == null) {
                continue;
            }

            if (!processedEmployees.add(employeeId)) {
                continue;
            }

            Employee employee =
                    employeeMap.get(employeeId);

            if (employee == null) {
                continue;
            }

            if (!matchesEmployeeSearch(
                    employee,
                    requestDTO.getSearchEmployee())) {

                continue;
            }

            BigDecimal wages =
                    record.getTotalEarnings();

            if (wages == null
                    || wages.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal employeeShare =
                    wages
                            .multiply(EMPLOYEE_RATE)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal employerShare =
                    wages
                            .multiply(EMPLOYER_RATE)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal total =
                    employeeShare
                            .add(employerShare)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            String department =
                    employee.getDepartment() != null
                            ? employee.getDepartment().getName()
                            : null;

            EsicEmployeeDetailsDTO dto =
                    employeeDetailsConverter.convertToDTO(
                            employee.getEmpCode(),
                            employee.getFormalName(),
                            generateMockEsicNumber(
                                    employee
                            ),
                            department,
                            wages,
                            employeeShare,
                            employerShare,
                            total,
                            "Active"
                    );

            response.add(dto);
        }

        response.sort(
                Comparator.comparing(
                        EsicEmployeeDetailsDTO::getEmployeeCode,
                        Comparator.nullsLast(
                                String::compareToIgnoreCase
                        )
                )
        );

        return new EsicEmployeeDetailsResponseDTO(
                summary,
                createEmployeePageResponse(
                        response,
                        requestDTO
                )
        );
    }

    public EsicEmployeeSummaryDTO getSummary(
            String financialYear,
            String month) {

        EsicChallanRequestDTO requestDTO =
                new EsicChallanRequestDTO();

        requestDTO.setFinancialYear(
                financialYear
        );

        requestDTO.setMonth(
                month
        );

        requestDTO.setPage(0);
        requestDTO.setSize(Integer.MAX_VALUE);

        return getEmployeeDetails(
                requestDTO
        ).getSummary();
    }

    private boolean matchesEmployeeSearch(
            Employee employee,
            String searchEmployee) {

        if (searchEmployee == null
                || searchEmployee.trim().isEmpty()) {

            return true;
        }

        String search =
                searchEmployee
                        .trim()
                        .toLowerCase();

        String employeeName =
                employee.getFormalName();

        String employeeCode =
                employee.getEmpCode();

        boolean nameMatch =
                employeeName != null
                        && employeeName
                        .toLowerCase()
                        .contains(search);

        boolean codeMatch =
                employeeCode != null
                        && employeeCode
                        .toLowerCase()
                        .contains(search);

        return nameMatch || codeMatch;
    }

    private String generateMockEsicNumber(
            Employee employee) {

        if (employee.getId() == null) {
            return null;
        }

        long number =
                Math.abs(
                        employee.getId()
                                .getMostSignificantBits()
                ) % 9000000000L;

        number += 1000000000L;

        return String.valueOf(number);
    }

    private String getMonthName(
            String monthYear) {

        if (monthYear == null
                || monthYear.length() != 7) {

            return monthYear;
        }

        int monthNumber;

        try {

            monthNumber =
                    Integer.parseInt(
                            monthYear.substring(5, 7)
                    );

        } catch (NumberFormatException e) {

            return monthYear;
        }

        if (monthNumber < 1
                || monthNumber > 12) {

            return monthYear;
        }

        return java.time.Month
                .of(monthNumber)
                .toString()
                .substring(0, 1)
                .toUpperCase()
                +
                java.time.Month
                        .of(monthNumber)
                        .toString()
                        .substring(1)
                        .toLowerCase();
    }

    private int getNextSequence() {

        return challanDAO
                .getGeneratedChallans(
                        null,
                        null
                )
                .stream()
                .mapToInt(challan -> {

                    if (challan.getChallanNumber() == null) {
                        return 0;
                    }

                    String[] parts =
                            challan.getChallanNumber()
                                    .split("/");

                    if (parts.length == 3) {

                        try {

                            return Integer.parseInt(
                                    parts[2]
                            );

                        } catch (NumberFormatException ignored) {
                        }
                    }

                    return 0;
                })
                .max()
                .orElse(0) + 1;
    }

    private boolean isBelowCeiling(
            BigDecimal wages) {

        return wages.compareTo(
                esicWageCeiling
        ) <= 0;
    }

    private void validateGenerateRequest(
            EsicChallanRequestDTO requestDTO) {

        if (requestDTO == null) {

            throw new RuntimeException(
                    "Request body is required"
            );
        }

        if (requestDTO.getFinancialYear() == null
                || requestDTO.getFinancialYear().trim().isEmpty()) {

            throw new RuntimeException(
                    "Financial year is required"
            );
        }

        if (!requestDTO.getFinancialYear()
                .matches("\\d{4}-\\d{2}")) {

            throw new RuntimeException(
                    "Financial year must be in format YYYY-YY"
            );
        }

        if (requestDTO.getMonth() == null
                || requestDTO.getMonth().trim().isEmpty()) {

            throw new RuntimeException(
                    "Month is required"
            );
        }
    }

    private PageResponse<EsicChallanListDTO>
    createPageResponse(
            List<EsicChallanListDTO> records,
            EsicChallanRequestDTO requestDTO) {

        int page =
                requestDTO.getPage() == null
                        ? 0
                        : Math.max(
                        0,
                        requestDTO.getPage()
                );

        int size =
                requestDTO.getSize() == null
                        ? 10
                        : Math.max(
                        1,
                        requestDTO.getSize()
                );

        int totalElements =
                records.size();

        int totalPages =
                totalElements == 0
                        ? 0
                        : (int) Math.ceil(
                        (double) totalElements / size
                );

        int fromIndex =
                page * size;

        if (fromIndex >= totalElements) {

            return new PageResponse<>(
                    Collections.emptyList(),
                    page,
                    size,
                    totalElements,
                    totalPages
            );
        }

        int toIndex =
                Math.min(
                        fromIndex + size,
                        totalElements
                );

        return new PageResponse<>(
                records.subList(
                        fromIndex,
                        toIndex
                ),
                page,
                size,
                totalElements,
                totalPages
        );
    }

    private PageResponse<EsicEmployeeDetailsDTO>
    createEmployeePageResponse(
            List<EsicEmployeeDetailsDTO> records,
            EsicChallanRequestDTO requestDTO) {

        int page =
                requestDTO.getPage() == null
                        ? 0
                        : Math.max(
                        0,
                        requestDTO.getPage()
                );

        int size =
                requestDTO.getSize() == null
                        ? 10
                        : Math.max(
                        1,
                        requestDTO.getSize()
                );

        int totalElements =
                records.size();

        int totalPages =
                totalElements == 0
                        ? 0
                        : (int) Math.ceil(
                        (double) totalElements / size
                );

        int fromIndex =
                page * size;

        if (fromIndex >= totalElements) {

            return new PageResponse<>(
                    Collections.emptyList(),
                    page,
                    size,
                    totalElements,
                    totalPages
            );
        }

        int toIndex =
                Math.min(
                        fromIndex + size,
                        totalElements
                );

        return new PageResponse<>(
                records.subList(
                        fromIndex,
                        toIndex
                ),
                page,
                size,
                totalElements,
                totalPages
        );
    }
}