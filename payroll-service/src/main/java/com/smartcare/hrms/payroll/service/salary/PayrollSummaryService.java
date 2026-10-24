package com.smartcare.hrms.payroll.service.salary;

import com.smartcare.hrms.common.entity.attendance.AttendanceSalaryLink;
import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.enumeration.PayrollStatus;
import com.smartcare.hrms.common.globalexception.ResourceConflictException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.payroll.config.ESICCalculator;
import com.smartcare.hrms.payroll.config.ProfessionalTaxCalculator;
import com.smartcare.hrms.payroll.config.SalaryCalculator;
import com.smartcare.hrms.payroll.dao.PayrollSummaryDashboardDAO;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDTO;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDtlsDTO;
import com.smartcare.hrms.common.dto.payroll.salary.BatchOperationDTO;
import com.smartcare.hrms.common.dto.payroll.salary.MonthlySalarySummaryDashboardDTO;
import com.smartcare.hrms.common.dto.payroll.salary.PayrollSummaryRequestDTO;
import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import com.smartcare.hrms.common.enumeration.CalculationType;
import com.smartcare.hrms.common.repository.payroll.salary.EmpMonthlyPayrollSummaryRepository;
import com.smartcare.hrms.payroll.service.compensation.component.ComponentMasterService;
import com.smartcare.hrms.payroll.service.compensation.employee.EmployeeCompensationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.smartcare.hrms.common.utility.PayrollMathUtil.roundAmount;

@Service
public class PayrollSummaryService {

    private @Autowired PayrollSummaryDashboardDAO employeeSalarySummaryDashboardDAO;
    private @Autowired EmpMonthlyPayrollSummaryRepository empMonthlySalaryRepository;
    private @Autowired EmployeeCompensationService employeeCompensationService;
    private @Autowired ComponentMasterService componentMasterService;


    public PaginatedDataResponse<MonthlySalarySummaryDashboardDTO> getMonthlyPayrollSummary(PayrollSummaryRequestDTO searchDTO) {
        PaginatedDataResponse<MonthlySalarySummaryDashboardDTO> payrollSummaryDashboard = employeeSalarySummaryDashboardDAO.getPayrollSummaryDashboard(searchDTO);
        List<String> employeeIds = payrollSummaryDashboard.getDataList().stream().map(summary -> summary.getEmployeeDetails().getEmployeeId()).toList();
        Map<String, EmpCompensationDTO> employeeCompensationMap = getEmployeeCompensationMap(employeeIds);
        payrollSummaryDashboard.getDataList().forEach(summary->{
            String employeeId = summary.getEmployeeDetails().getEmployeeId();
            if(employeeCompensationMap.containsKey(employeeId)){
                MonthlySalarySummaryDashboardDTO.ProcessStatus status = summary.getStatus();
                EmpCompensationDTO empCompensationDTO = employeeCompensationMap.get(employeeId);
                setMonthlyGross(summary,empCompensationDTO);
                if(AttendanceStatusEnum.LOCKED.equals(status.getAttendanceStatus()) && PayrollStatus.PENDING.equals(status.getPayrollStatus())){
                    // calculate initial salary at runtime
                    calculateInitialEmployeeSalary(summary,employeeCompensationMap.get(summary.getEmployeeDetails().getEmployeeId()));
                }
            }
        });
        return payrollSummaryDashboard;
    }


    public void create(List<BatchOperationDTO> linkIds){
        List<EmpMonthlyPayrollSummary> payrollSummaries = new ArrayList<>();

        List<EmpMonthlyPayrollSummary> byAttendanceSalaryLinkIdIn = empMonthlySalaryRepository.findByAttendanceSalaryLinkIdIn(linkIds.stream().map(linkId -> UUID.fromString(linkId.getAttendanceSalaryLinkId())).toList());
        Map<UUID, EmpMonthlyPayrollSummary> attendanceLinkIdToPayrollSummaryMap = byAttendanceSalaryLinkIdIn.stream().collect(Collectors.toMap(summary -> summary.getAttendanceSalaryLink().getId(), Function.identity()));
        for (BatchOperationDTO linkId : linkIds) {
            if(attendanceLinkIdToPayrollSummaryMap.containsKey(UUID.fromString(linkId.getAttendanceSalaryLinkId()))){
                EmpMonthlyPayrollSummary payrollSummary = attendanceLinkIdToPayrollSummaryMap.get(UUID.fromString(linkId.getAttendanceSalaryLinkId()));
                payrollSummary.setPayableDays(linkId.getPayableDays());
                payrollSummary.setLossOfPayDays(linkId.getLossOfPayDays());
                payrollSummaries.add(payrollSummary);
            }else{
                EmpMonthlyPayrollSummary payrollSummary = new EmpMonthlyPayrollSummary();
                AttendanceSalaryLink attendanceSalaryLink = new AttendanceSalaryLink();
                attendanceSalaryLink.setId(UUID.fromString(linkId.getAttendanceSalaryLinkId()));
                payrollSummary.setAttendanceSalaryLink(attendanceSalaryLink);
                payrollSummary.setPayableDays(linkId.getPayableDays());
                payrollSummary.setLossOfPayDays(linkId.getLossOfPayDays());
                payrollSummaries.add(payrollSummary);
            }
        }
        empMonthlySalaryRepository.saveAll(payrollSummaries);
    }
    public void lockMonthlyPayroll(List<String> payrollIds) {
        List<EmpMonthlyPayrollSummary> summaryList = empMonthlySalaryRepository.findByPayrollIdIn(payrollIds.stream().map(UUID::fromString).toList());
        List<EmpMonthlyPayrollSummary> invalidToLockPayrollSummaries = new ArrayList<>();
        summaryList.stream().filter(summary -> !PayrollStatus.DRAFT.equals(summary.getAttendanceSalaryLink().getPayrollStatus())).forEach(invalidToLockPayrollSummaries::add);
        if (!invalidToLockPayrollSummaries.isEmpty()) {
            throw new ResourceConflictException("Only Drafted payroll details can be locked. Invalid payroll count: " + invalidToLockPayrollSummaries.size());
        }
        summaryList.forEach(summary -> {
            summary.getAttendanceSalaryLink().setPayrollStatus(PayrollStatus.LOCKED);
            summary.getAttendanceSalaryLink().setAttendanceStatus(AttendanceStatusEnum.FROZEN);
        });
        empMonthlySalaryRepository.saveAll(summaryList);
    }

    public void rollBackToDraft(List<String> payrollIds) {
        List<EmpMonthlyPayrollSummary> summaryList = empMonthlySalaryRepository.findByPayrollIdIn(payrollIds.stream().map(UUID::fromString).toList());
        List<EmpMonthlyPayrollSummary> invalidToLockPayrollSummaries = new ArrayList<>();
        summaryList.stream().filter(summary -> !PayrollStatus.LOCKED.equals(summary.getAttendanceSalaryLink().getPayrollStatus())).forEach(invalidToLockPayrollSummaries::add);
        if (!invalidToLockPayrollSummaries.isEmpty()) {
            throw new ResourceConflictException("Only Locked payroll details can be roll back to Draft. Invalid payroll count: " + invalidToLockPayrollSummaries.size());
        }
        summaryList.forEach(summary -> {
            summary.getAttendanceSalaryLink().setPayrollStatus(PayrollStatus.DRAFT);
            summary.getAttendanceSalaryLink().setAttendanceStatus(AttendanceStatusEnum.LOCKED);
        });
        empMonthlySalaryRepository.saveAll(summaryList);
    }

    public void rollBackToEstimate(List<String> payrollIds) {
        List<EmpMonthlyPayrollSummary> summaryList = empMonthlySalaryRepository.findByPayrollIdIn(payrollIds.stream().map(UUID::fromString).toList());
        List<EmpMonthlyPayrollSummary> invalidToLockPayrollSummaries = new ArrayList<>();
        summaryList.stream().filter(summary -> !PayrollStatus.DRAFT.equals(summary.getAttendanceSalaryLink().getPayrollStatus())).forEach(invalidToLockPayrollSummaries::add);
        if (!invalidToLockPayrollSummaries.isEmpty()) {
            throw new ResourceConflictException("Only Draft payroll details can be roll back to Estimate. Invalid payroll count: " + invalidToLockPayrollSummaries.size());
        }
        summaryList.forEach(summary -> {
            BigDecimal zero = roundAmount(BigDecimal.ZERO);
            summary.setTotalEarnings(zero);
            summary.setTotalStandardDeductions(zero);
            summary.setTotalOtherDeductions(zero);
            summary.setEmpCompensationHdr(null);
            summary.setTotalPayableEarnings(zero);
            summary.setTotalPayableOtherDeductions(zero);
            summary.setTotalPayableStandardDeductions(zero);
            summary.setNetPayable(zero);
            summary.setSalaryComponents(null);
            summary.getAttendanceSalaryLink().setPayrollStatus(PayrollStatus.DRAFT);
            summary.getAttendanceSalaryLink().setAttendanceStatus(AttendanceStatusEnum.LOCKED);
        });
        empMonthlySalaryRepository.saveAll(summaryList);
    }



    public void publish(List<String> payrollIds) {
        List<EmpMonthlyPayrollSummary> summaryList = empMonthlySalaryRepository.findByPayrollIdIn(payrollIds.stream().map(UUID::fromString).toList());
        List<EmpMonthlyPayrollSummary> invalidToLockPayrollSummaries = new ArrayList<>();
        summaryList.stream().filter(summary -> !PayrollStatus.LOCKED.equals(summary.getAttendanceSalaryLink().getPayrollStatus())).forEach(invalidToLockPayrollSummaries::add);
        if (!invalidToLockPayrollSummaries.isEmpty()) {
            throw new ResourceConflictException("Only Locked payroll details can be Publish. Invalid payroll count: " + invalidToLockPayrollSummaries.size());
        }
        summaryList.forEach(summary -> {
            summary.getAttendanceSalaryLink().setPayrollStatus(PayrollStatus.PUBLISHED);
            summary.getAttendanceSalaryLink().setAttendanceStatus(AttendanceStatusEnum.FROZEN);
        });
        empMonthlySalaryRepository.saveAll(summaryList);
    }



    private Map<String, EmpCompensationDTO> getEmployeeCompensationMap(List<String> employeeIds) {
        List<EmpCompensationDTO> compensationList = employeeCompensationService.getIdIn(employeeIds);
        return compensationList.stream().collect(Collectors.toMap(EmpCompensationDTO::getEmployeeId, Function.identity()));
    }

    private void setMonthlyGross(MonthlySalarySummaryDashboardDTO summary,EmpCompensationDTO compensationDTO) {
        summary.setMonthlyGross(
         MonthlySalarySummaryDashboardDTO.MonthlyGross.builder()
                .totalEarnings(compensationDTO.getGrossAmount())
                .totalStdDeductions(compensationDTO.getStandardDeductionAmount())
                .totalOtherDeductions(compensationDTO.getOtherDeductionAmount())
                .build()
        );
    }

    private void calculateInitialEmployeeSalary(MonthlySalarySummaryDashboardDTO summaryDTO,EmpCompensationDTO compensationDTO){
        MonthlySalarySummaryDashboardDTO.DaysDetails daysDetails=summaryDTO.getDaysDetails();
        MonthlySalarySummaryDashboardDTO.MonthlyGross monthlyGross = summaryDTO.getMonthlyGross();
        MonthlySalarySummaryDashboardDTO.CurrentMonthPayable currentMonthPayable = new MonthlySalarySummaryDashboardDTO.CurrentMonthPayable();

        //calculate salary at runtime based on compensation details and days details
        BigDecimal daysInMonth = BigDecimal.valueOf(daysDetails.getTotalDaysInMonth());
        BigDecimal payableDays = daysDetails.getPayableDays() != null
                ? BigDecimal.valueOf(daysDetails.getPayableDays())
                : BigDecimal.ZERO;

        BigDecimal lossOfPayDays = daysDetails.getLossOfPayDays() != null
                ? BigDecimal.valueOf(daysDetails.getLossOfPayDays())
                : BigDecimal.ZERO;
        Map<String,Double> earningsComponentAmountMap = new HashMap<>();
        BigDecimal totalPerDayEarnings = BigDecimal.ZERO;
        {
            //Earning calculated based on per day amount and payable days
            List<@Valid EmpCompensationDtlsDTO> earningList = compensationDTO.getCompensationDetails().stream().filter(component -> component.getComponentType().equals(ComponentType.EARNINGS)).toList();
            BigDecimal totalMonthlyGrossEarnings = summaryDTO.getNsaAmount();
            for (EmpCompensationDtlsDTO compensationDetail : earningList) {
                //per day component amount as per total days in month
                BigDecimal perdayComponentAmount = compensationDetail.getAmount().divide(daysInMonth, 10, RoundingMode.HALF_UP);

                //total of per day amount as per total dauys in month for all earning components to calculate loss of pay
                totalPerDayEarnings = totalPerDayEarnings.add(perdayComponentAmount);

                BigDecimal currentMonthEarningAmount = roundAmount(perdayComponentAmount.multiply(payableDays));
                //addingcurrent month  per day amount to map for later use in deduction calculation
                earningsComponentAmountMap.put(compensationDetail.getCode(), currentMonthEarningAmount.doubleValue());
                compensationDetail.setAmount(currentMonthEarningAmount);
                totalMonthlyGrossEarnings  = totalMonthlyGrossEarnings.add(currentMonthEarningAmount);
            }
            currentMonthPayable.setTotalEarnings(totalMonthlyGrossEarnings);
        }
        {
            //Calculate Standard Deduction based on per day amount and loss of pay days
            List<@Valid EmpCompensationDtlsDTO> standardDeductionList = compensationDTO.getCompensationDetails().stream().filter(component -> component.getComponentType().equals(ComponentType.STATUTORY_DEDUCTIONS)).toList();
            BigDecimal stdDeductionTotal = BigDecimal.ZERO;
            for (EmpCompensationDtlsDTO empCompensationDtlsDTO : standardDeductionList) {
                BigDecimal deductionAmount = BigDecimal.ZERO;
                if(empCompensationDtlsDTO.getCode().equals("PT")){
                    deductionAmount = ProfessionalTaxCalculator.calculateProfessionalTax(currentMonthPayable.getTotalEarnings(), summaryDTO.getEmployeeDetails().getPayrollState(), summaryDTO.getEmployeeDetails().getGender().toString());
                }else if(empCompensationDtlsDTO.getCode().equals("ESIC")){
                    deductionAmount = ESICCalculator.calculateESI(currentMonthPayable.getTotalEarnings());
                }else if(empCompensationDtlsDTO.getCode().equals("PF")){
                    if(empCompensationDtlsDTO.getCalculationType().equals(CalculationType.FIXED)){
                        deductionAmount =empCompensationDtlsDTO.getAmount();
                    }else{
                        EmpCompensationDtlsDTO.FormulaDTO formulaDTO = empCompensationDtlsDTO.getFormulaDTO();
                        Map<String, Double> vars = formulaDTO.getVars();
                        vars.putAll(earningsComponentAmountMap);
                        deductionAmount = SalaryCalculator.evaluate(formulaDTO.getExpression(), vars);
                    }
                }
                if(empCompensationDtlsDTO.getThresholdAmount() != null && deductionAmount.compareTo(empCompensationDtlsDTO.getThresholdAmount()) > 0){
                    deductionAmount = empCompensationDtlsDTO.getThresholdAmount();
                }
                stdDeductionTotal = stdDeductionTotal.add(roundAmount(deductionAmount));
                empCompensationDtlsDTO.setAmount(deductionAmount);
            }
            currentMonthPayable.setTotalStdDeductions(roundAmount(stdDeductionTotal));
        }
        {
            //calculate other deduction based on per day amount and loss of pay days
            List<@Valid EmpCompensationDtlsDTO> standardDeductionList = compensationDTO.getCompensationDetails().stream().filter(component -> component.getComponentType().equals(ComponentType.DEDUCTIONS)).toList();
            BigDecimal otherDeductionTotal = BigDecimal.ZERO;
            for (EmpCompensationDtlsDTO empCompensationDtlsDTO : standardDeductionList) {
                if(empCompensationDtlsDTO.getCalculationType().equals(CalculationType.FIXED)){
                    otherDeductionTotal = otherDeductionTotal.add(empCompensationDtlsDTO.getAmount());
                }else{
                    EmpCompensationDtlsDTO.FormulaDTO formulaDTO = empCompensationDtlsDTO.getFormulaDTO();
                    Map<String, Double> vars = formulaDTO.getVars();
                    vars.putAll(earningsComponentAmountMap);
                    BigDecimal deductionAmount = SalaryCalculator.evaluate(formulaDTO.getExpression(), vars);
                    otherDeductionTotal = otherDeductionTotal.add(roundAmount(deductionAmount));
                    empCompensationDtlsDTO.setAmount(deductionAmount);
                }
            }
            currentMonthPayable.setTotalOtherDeductions(roundAmount(otherDeductionTotal));
        }
        {
            //calculate loss of pay based on per day amount and loss of pay days
            BigDecimal lossOfPayAmount = roundAmount(totalPerDayEarnings.multiply(lossOfPayDays));
            currentMonthPayable.setLossOfPayAmount(lossOfPayAmount);
        }
        BigDecimal netPaySalary = currentMonthPayable.getTotalEarnings().subtract(currentMonthPayable.getTotalStdDeductions()).subtract(currentMonthPayable.getTotalOtherDeductions());
        currentMonthPayable.setNetPayable(netPaySalary);
        summaryDTO.setCurrentMonthPayable(currentMonthPayable);
    }



}
