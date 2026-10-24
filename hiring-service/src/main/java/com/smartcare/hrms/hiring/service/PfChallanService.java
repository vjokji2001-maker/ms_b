package com.smartcare.hrms.hiring.service;

import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.hiring.converter.PfChallanConverter;
import com.smartcare.hrms.hiring.converter.PfEmployeeDetailsConverter;
import com.smartcare.hrms.hiring.dao.PfChallanDAO;
import com.smartcare.hrms.hiring.dto.*;
import com.smartcare.hrms.hiring.model.entity.PfChallan;
import com.smartcare.hrms.hiring.model.entity.PfChallanEmployee;
import com.smartcare.hrms.hiring.repository.PfChallanEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PfChallanService {

    @Value("${pf.employee.rate}")
    private BigDecimal employeePfRate;

    @Value("${pf.employer.rate}")
    private BigDecimal employerPfRate;

    @Value("${pf.eps.rate}")
    private BigDecimal epsRate;

    private @Autowired
    PfChallanDAO challanDAO;

    private @Autowired
    EmployeeRepository employeeRepository;

    private @Autowired
    PfChallanConverter challanConverter;

    private @Autowired
    PfEmployeeDetailsConverter employeeDetailsConverter;

    private @Autowired
    PfChallanEmployeeRepository challanEmployeeRepository;

    public PageResponse<PfChallanListDTO> getChallanList(
            PfChallanRequestDTO requestDTO) {

        List<PfChallan> challans =
                challanDAO.getGeneratedChallans(
                        requestDTO.getFinancialYear(),
                        requestDTO.getMonth()
                );

        List<PfChallanListDTO> response =
                new ArrayList<>();

        for (PfChallan challan : challans) {

            response.add(
                    challanConverter.convertToDTO(
                            challan.getChallanNumber(),
                            challan.getMonth(),
                            challan.getFinancialYear(),
                            challan.getTotalEmployees(),
                            challan.getEmployeePF(),
                            challan.getEmployerPF(),
                            challan.getEps(),
                            challan.getTotalAmount(),
                            challan.getStatus()
                    )
            );

            response.get(response.size() - 1)
                    .setGeneratedOn(
                            challan.getGeneratedOn()
                    );
        }

        return createPageResponse(
                response,
                requestDTO
        );
    }

    @Transactional
    public PfChallanListDTO generateChallan(
            PfChallanRequestDTO requestDTO) {

        validateGenerateRequest(requestDTO);

        String monthYear =
                challanDAO.getResolvedMonthYear(
                        requestDTO.getMonth(),
                        requestDTO.getFinancialYear()
                );

        PfChallan existing =
                challanDAO.getExistingChallan(
                        requestDTO.getFinancialYear(),
                        monthYear
                );

        if (existing != null) {

            throw new RuntimeException(
                    "PF challan already generated for "
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

        BigDecimal totalEmployeePF =
                BigDecimal.ZERO;

        BigDecimal totalEmployerPF =
                BigDecimal.ZERO;

        BigDecimal totalEPS =
                BigDecimal.ZERO;

        int totalEmployees = 0;

        Set<String> processedEmployees =
                new HashSet<>();

        List<PfChallanEmployee> employeeDetails =
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

            BigDecimal basicDA =
                    getBasicDA(record);

            if (basicDA == null
                    || basicDA.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal employeePF =
                    calculate(
                            basicDA,
                            employeePfRate
                    );

            BigDecimal employerPF =
                    calculate(
                            basicDA,
                            employerPfRate
                    );

            BigDecimal eps =
                    calculate(
                            basicDA,
                            epsRate
                    );

            BigDecimal total =
                    employeePF
                            .add(employerPF)
                            .add(eps)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            totalEmployeePF =
                    totalEmployeePF.add(employeePF);

            totalEmployerPF =
                    totalEmployerPF.add(employerPF);

            totalEPS =
                    totalEPS.add(eps);

            totalEmployees++;

            String designation =
                    employee.getDesignation() != null
                            ? employee.getDesignation().getName()
                            : null;

            String department =
                    employee.getDepartment() != null
                            ? employee.getDepartment().getName()
                            : null;

            PfChallanEmployee detail =
                    new PfChallanEmployee();

            detail.setEmployeeCode(
                    employee.getEmpCode()
            );

            detail.setEmployeeName(
                    employee.getFormalName()
            );

            detail.setDesignation(
                    designation
            );

            detail.setUan(
                    generateMockUAN(employee)
            );

            detail.setPfNumber(
                    generateMockPfNumber(employee)
            );

            detail.setDepartment(
                    department
            );

            detail.setBasicDA(
                    basicDA.setScale(
                            2,
                            RoundingMode.HALF_UP
                    )
            );

            detail.setEmployeePF(employeePF);
            detail.setEmployerPF(employerPF);
            detail.setEps(eps);
            detail.setTotal(total);
            detail.setStatus("Active");

            employeeDetails.add(detail);
        }

        if (totalEmployees == 0) {

            throw new RuntimeException(
                    "No valid employee payroll data found for "
                            + requestDTO.getMonth()
            );
        }

        totalEmployeePF =
                totalEmployeePF.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        totalEmployerPF =
                totalEmployerPF.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        totalEPS =
                totalEPS.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal totalAmount =
                totalEmployeePF
                        .add(totalEmployerPF)
                        .add(totalEPS)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        int nextSequence =
                getNextSequence();

        String challanNumber =
                String.format(
                        "PF/%s/%03d",
                        requestDTO.getFinancialYear(),
                        nextSequence
                );

        PfChallan challan =
                new PfChallan();

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

        challan.setEmployeePF(
                totalEmployeePF
        );

        challan.setEmployerPF(
                totalEmployerPF
        );

        challan.setEps(
                totalEPS
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

        PfChallan savedChallan =
                challanDAO.saveChallan(
                        challan
                );

        for (PfChallanEmployee detail :
                employeeDetails) {

            detail.setChallanId(
                    savedChallan.getId()
            );
        }

        challanEmployeeRepository.saveAll(
                employeeDetails
        );

        PfChallanListDTO response =
                challanConverter.convertToDTO(
                        savedChallan.getChallanNumber(),
                        savedChallan.getMonth(),
                        savedChallan.getFinancialYear(),
                        savedChallan.getTotalEmployees(),
                        savedChallan.getEmployeePF(),
                        savedChallan.getEmployerPF(),
                        savedChallan.getEps(),
                        savedChallan.getTotalAmount(),
                        savedChallan.getStatus()
                );

        response.setGeneratedOn(
                savedChallan.getGeneratedOn()
        );

        return response;
    }

    public PfEmployeeDetailsResponseDTO getEmployeeDetails(
            PfChallanRequestDTO requestDTO) {

        List<EmpMonthlyPayrollSummary> payrollRecords =
                challanDAO.getEmployeePayrollData(
                        requestDTO.getFinancialYear(),
                        requestDTO.getMonth()
                );

        if (payrollRecords == null
                || payrollRecords.isEmpty()) {

            PfEmployeeSummaryDTO summary =
                    new PfEmployeeSummaryDTO(
                            0,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO
                    );

            PageResponse<PfEmployeeDetailsDTO> emptyPage =
                    createEmployeePageResponse(
                            new ArrayList<>(),
                            requestDTO
                    );

            return new PfEmployeeDetailsResponseDTO(
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

        BigDecimal totalEmployeePF =
                BigDecimal.ZERO;

        BigDecimal totalEmployerPF =
                BigDecimal.ZERO;

        BigDecimal totalEPS =
                BigDecimal.ZERO;

        int totalEmployees = 0;

        List<PfEmployeeDetailsDTO> response =
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

            BigDecimal basicDA =
                    getBasicDA(record);

            if (basicDA == null
                    || basicDA.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal employeePF =
                    calculate(
                            basicDA,
                            employeePfRate
                    );

            BigDecimal employerPF =
                    calculate(
                            basicDA,
                            employerPfRate
                    );

            BigDecimal eps =
                    calculate(
                            basicDA,
                            epsRate
                    );

            BigDecimal total =
                    employeePF
                            .add(employerPF)
                            .add(eps)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            totalEmployeePF =
                    totalEmployeePF.add(employeePF);

            totalEmployerPF =
                    totalEmployerPF.add(employerPF);

            totalEPS =
                    totalEPS.add(eps);

            totalEmployees++;

            String designation =
                    employee.getDesignation() != null
                            ? employee.getDesignation().getName()
                            : null;

            String department =
                    employee.getDepartment() != null
                            ? employee.getDepartment().getName()
                            : null;

            PfEmployeeDetailsDTO dto =
                    employeeDetailsConverter.convertToDTO(
                            employee.getEmpCode(),
                            employee.getFormalName(),
                            designation,
                            generateMockUAN(employee),
                            generateMockPfNumber(employee),
                            department,
                            basicDA,
                            employeePF,
                            employerPF,
                            eps,
                            total,
                            "Active"
                    );

            response.add(dto);
        }

        response.sort(
                Comparator.comparing(
                        PfEmployeeDetailsDTO::getEmployeeCode,
                        Comparator.nullsLast(
                                String::compareToIgnoreCase
                        )
                )
        );

        PfEmployeeSummaryDTO summary =
                new PfEmployeeSummaryDTO(
                        totalEmployees,
                        totalEmployeePF.setScale(
                                2,
                                RoundingMode.HALF_UP
                        ),
                        totalEmployerPF.setScale(
                                2,
                                RoundingMode.HALF_UP
                        ),
                        totalEPS.setScale(
                                2,
                                RoundingMode.HALF_UP
                        ),
                        totalEmployeePF
                                .add(totalEmployerPF)
                                .add(totalEPS)
                                .setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                );

        return new PfEmployeeDetailsResponseDTO(
                summary,
                createEmployeePageResponse(
                        response,
                        requestDTO
                )
        );
    }

    public PfEmployeeSummaryDTO getSummary(
            String financialYear,
            String month) {

        PfChallanRequestDTO requestDTO =
                new PfChallanRequestDTO();

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

    private BigDecimal getBasicDA(
            EmpMonthlyPayrollSummary record) {

        return record.getTotalEarnings();
    }

    private BigDecimal calculate(
            BigDecimal value,
            BigDecimal rate) {

        return value
                .multiply(rate)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
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

    private String generateMockUAN(
            Employee employee) {

        if (employee.getId() == null) {
            return null;
        }

        long number =
                Math.abs(
                        employee.getId()
                                .getMostSignificantBits()
                ) % 900000000000L;

        number += 100000000000L;

        return String.valueOf(number);
    }

    private String generateMockPfNumber(
            Employee employee) {

        return "PF/" + employee.getEmpCode();
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

    private void validateGenerateRequest(
            PfChallanRequestDTO requestDTO) {

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

    private PageResponse<PfChallanListDTO>
    createPageResponse(
            List<PfChallanListDTO> records,
            PfChallanRequestDTO requestDTO) {

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

    private PageResponse<PfEmployeeDetailsDTO>
    createEmployeePageResponse(
            List<PfEmployeeDetailsDTO> records,
            PfChallanRequestDTO requestDTO) {

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