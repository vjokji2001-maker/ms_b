package com.smartcare.hrms.payroll.service.salary;

import com.smartcare.hrms.common.entity.attendance.AttendanceSalaryLink;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.master.common.City;
import com.smartcare.hrms.common.entity.master.common.State;
import com.smartcare.hrms.common.enumeration.PayrollStatus;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.common.CityRepository;
import com.smartcare.hrms.common.repository.master.common.StateRepository;
import com.smartcare.hrms.common.utility.PayrollMathUtil;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDTO;
import com.smartcare.hrms.common.dto.payroll.salary.EmployeeSalaryDetailsDTO;
import com.smartcare.hrms.payroll.dto.statutory.EmployeeStatutoryDTO;
import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import com.smartcare.hrms.common.entity.payroll.statutory.EmployeeStatutoryInfo;
import com.smartcare.hrms.payroll.mappers.compensation.employee.EmpCompensationMapper;
import com.smartcare.hrms.payroll.mappers.compensation.employee.EmployeeStatutoryMapper;
import com.smartcare.hrms.payroll.mappers.compensation.salary.EmployeeSalaryConverter;
import com.smartcare.hrms.common.repository.payroll.salary.EmpMonthlyPayrollSummaryRepository;
import com.smartcare.hrms.common.repository.payroll.statutory.EmployeeStatutoryInfoRepository;
import com.smartcare.hrms.payroll.service.compensation.employee.EmployeeCompensationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class EmployeeMonthlySalaryService {

    private @Autowired EmpMonthlyPayrollSummaryRepository empMonthlySalaryRepository;
    private @Autowired EmployeeCompensationService employeeCompensationService;
    private @Autowired EmployeeStatutoryInfoRepository employeeStatutoryInfoRepository;
    private @Autowired EmployeeStatutoryMapper employeeStatutoryMapper;
    private @Autowired SalaryCalculationService salaryCalculationService;
    private @Autowired EmployeeSalaryConverter employeeSalaryConverter;
    private @Autowired EmpCompensationMapper empCompensationMapper;
    private @Autowired EmployeeRepository employeeRepository;
    private @Autowired StateRepository stateRepository;
    private @Autowired CityRepository cityRepository;

    @Value("${server.base.url}")
    private String serverBaseUrl;

    /** get individuL employee salary with details **/
    public EmployeeSalaryDetailsDTO getMonthSalaryDetails(String payrollId) {
        EmpMonthlyPayrollSummary monthlyEmployeePayrollRecord = empMonthlySalaryRepository.findByIdWithLink(UUID.fromString(payrollId))
                .orElseThrow(() -> new ValidationException("Payroll record not found "));
        Employee employee = employeeRepository.findById(UUID.fromString(monthlyEmployeePayrollRecord.getAttendanceSalaryLink().getEmployeeId()))
                .orElseThrow(() -> new ValidationException("Employee not found"));
        EmployeeStatutoryInfo employeeStatutoryInfo = employeeStatutoryInfoRepository.findByEmployeeId(UUID.fromString(monthlyEmployeePayrollRecord.getAttendanceSalaryLink().getEmployeeId()))
                .orElseThrow(() -> new ValidationException("Statutory info not found for employee id: " + monthlyEmployeePayrollRecord.getAttendanceSalaryLink().getEmployeeId()));
        EmployeeStatutoryDTO statutoryDTO = employeeStatutoryMapper.toDTO(employeeStatutoryInfo);
        if(!CollectionUtils.isEmpty(monthlyEmployeePayrollRecord.getSalaryComponents())){
            EmployeeSalaryDetailsDTO dto = employeeSalaryConverter.toDTO(monthlyEmployeePayrollRecord);
            BigDecimal lossOfPayAmount = PayrollMathUtil.roundAmount(BigDecimal.ZERO);
            dto.getDeductions().stream().filter(f->f.getComponentCode().equals("LOP")).findFirst().ifPresent(lop-> dto.setLossOfPayAmount(lop.getPayableAmount()));
            dto.setLossOfPayAmount(lossOfPayAmount);
            YearMonth yearMonth = YearMonth.parse(monthlyEmployeePayrollRecord.getAttendanceSalaryLink().getMonthYear(), DateTimeFormatter.ofPattern("yyyy-MM", Locale.ENGLISH));
            Double daysInMonth = (double) yearMonth.lengthOfMonth();
            dto.setDaysInMonth(daysInMonth);
            dto.setStatus(monthlyEmployeePayrollRecord.getAttendanceSalaryLink().getPayrollStatus().getValue());
            dto.setEmployeeName(employee.getFormalName());
            dto.setEmployeeCode(employee.getEmpCode());
            dto.setPersonalEmail(employee.getPersonalEmail());
            dto.setGrade(employee.getGrade() != null ? employee.getGrade().getName() : null);
            dto.setCompanyName(employee.getCompany()!=null?employee.getCompany().getName():null);
            dto.setRegisteredName(employee.getCompany()!=null?employee.getCompany().getRegisteredName():null);
            dto.setAddress(employee.getCompany()!=null?employee.getCompany().getAddress():null);
            dto.setPincode(employee.getCompany()!=null?employee.getCompany().getPincode():null);
            dto.setImageUrl(employee.getCompany()!=null?serverBaseUrl+employee.getCompany().getImageUrl():null);
            dto.setEmail(employee.getCompany()!=null?employee.getCompany().getEmail():null);
            String stateName = employee.getCompany() != null && employee.getCompany().getStateId() != null
                    ? stateRepository.findById(Long.valueOf(employee.getCompany().getStateId()))
                    .map(State::getName)
                    .orElse(null)
                    : null;

            String cityName = employee.getCompany() != null && employee.getCompany().getCityId() != null
                    ? cityRepository.findById(Long.valueOf(employee.getCompany().getCityId()))
                    .map(City::getName)
                    .orElse(null)
                    : null;
            dto.setStateName(stateName);
            dto.setCityName(cityName);
            dto.setPayrollState(employeeStatutoryInfo.getPayrollState());
            return dto;
        }else{
            EmpCompensationDTO empCompensationDTO = employeeCompensationService.getById(monthlyEmployeePayrollRecord.getAttendanceSalaryLink().getEmployeeId());
            AttendanceSalaryLink attendanceSalaryLink = monthlyEmployeePayrollRecord.getAttendanceSalaryLink();
            String monthYear = attendanceSalaryLink.getMonthYear();

            YearMonth yearMonth = YearMonth.parse(monthYear, DateTimeFormatter.ofPattern("yyyy-MM", Locale.ENGLISH));
            Double daysInMonth = (double) yearMonth.lengthOfMonth();

            EmployeeSalaryDetailsDTO employeeSalaryDetailsDTO = EmployeeSalaryDetailsDTO.builder()
                    .status(PayrollStatus.ESTIMATED.getValue())
                    .daysInMonth(daysInMonth)
                    .payableDays(monthlyEmployeePayrollRecord.getPayableDays())
                    .lossOfPayDays(monthlyEmployeePayrollRecord.getLossOfPayDays())
                    .payrollId(monthlyEmployeePayrollRecord.getId().toString())
                    .employeeId(monthlyEmployeePayrollRecord.getAttendanceSalaryLink().getEmployeeId())
                    .yearMonth(monthlyEmployeePayrollRecord.getAttendanceSalaryLink().getMonthYear())
                    .employeeName(employee.getFormalName())
                    .employeeCode(employee.getEmpCode())
                    .grade(employee.getGrade() != null ? employee.getGrade().getName() : null)
                    .build();
            return salaryCalculationService.calculateSalaryOnCompensation(empCompensationDTO,statutoryDTO,employeeSalaryDetailsDTO,attendanceSalaryLink);
        }
    }


    public void draftMonthlySalary(EmployeeSalaryDetailsDTO employeeSalaryDetailsDTO) {
        EmpMonthlyPayrollSummary employeesalary = empMonthlySalaryRepository.findByIdWithLink(UUID.fromString(employeeSalaryDetailsDTO.getPayrollId()))
                .orElseThrow(() -> new ValidationException("Payroll record not found "));

        EmployeeStatutoryInfo employeeStatutoryInfo = employeeStatutoryInfoRepository.findByEmployeeId(UUID.fromString(employeesalary.getAttendanceSalaryLink().getEmployeeId()))
                .orElseThrow(() -> new ValidationException("Statutory info not found for employee id: " + employeesalary.getAttendanceSalaryLink().getEmployeeId()));
        EmployeeStatutoryDTO statutoryDTO = employeeStatutoryMapper.toDTO(employeeStatutoryInfo);
        EmpCompensationDTO empCompensationDTO = employeeCompensationService.getById(employeesalary.getAttendanceSalaryLink().getEmployeeId());
        String monthYear = employeesalary.getAttendanceSalaryLink().getMonthYear();
        YearMonth yearMonth = YearMonth.parse(monthYear, DateTimeFormatter.ofPattern("yyyy-MM", Locale.ENGLISH));
        employeeSalaryDetailsDTO.setDaysInMonth((double) yearMonth.lengthOfMonth());
        salaryCalculationService.validateInComingSalary(employeeSalaryDetailsDTO,empCompensationDTO,statutoryDTO);
        employeeSalaryConverter.updateEntityFromDto(employeeSalaryDetailsDTO,employeesalary);
        employeesalary.setTotalEarnings(empCompensationDTO.getGrossAmount());
        employeesalary.setTotalStandardDeductions(empCompensationDTO.getStandardDeductionAmount());
        employeesalary.setTotalOtherDeductions(empCompensationDTO.getOtherDeductionAmount());
        employeesalary.setEmpCompensationHdr(empCompensationDTO);
        employeesalary.getAttendanceSalaryLink().setPayrollStatus(PayrollStatus.DRAFT);
        empMonthlySalaryRepository.save(employeesalary);
    }

}
