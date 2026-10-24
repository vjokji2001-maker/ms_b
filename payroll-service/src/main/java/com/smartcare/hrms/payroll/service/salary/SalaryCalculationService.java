package com.smartcare.hrms.payroll.service.salary;

import com.smartcare.hrms.common.entity.attendance.AttendanceSalaryLink;
import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.payroll.config.ESICCalculator;
import com.smartcare.hrms.payroll.config.ProfessionalTaxCalculator;
import com.smartcare.hrms.payroll.config.SalaryCalculator;
import com.smartcare.hrms.common.dto.payroll.compensation.component.ComponentDTO;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDTO;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDtlsDTO;
import com.smartcare.hrms.common.dto.payroll.salary.EmployeeSalaryComponentDTO;
import com.smartcare.hrms.common.dto.payroll.salary.EmployeeSalaryDetailsDTO;
import com.smartcare.hrms.payroll.dto.statutory.EmployeeStatutoryDTO;
import com.smartcare.hrms.common.entity.payroll.compensation.employee.Formula;
import com.smartcare.hrms.common.enumeration.CalculationType;
import com.smartcare.hrms.common.enumeration.SalaryComponentSource;
import com.smartcare.hrms.payroll.mappers.compensation.salary.EmployeeSalaryConverter;
import com.smartcare.hrms.payroll.service.compensation.component.ComponentMasterService;
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
public class SalaryCalculationService {

    private @Autowired EmployeeSalaryConverter employeeSalaryConverter;
    private @Autowired ComponentMasterService componentMasterService;
    /*
    This method will be called when fetching employee salary for the first time in payroll summary or the salary is nogt saved for the current onth
     */
    public EmployeeSalaryDetailsDTO calculateSalaryOnCompensation(EmpCompensationDTO compensationDTO, EmployeeStatutoryDTO statutoryDTO, EmployeeSalaryDetailsDTO monthlySalaryDTO, AttendanceSalaryLink attendanceSalaryLink) {
        BigDecimal payableDays = BigDecimal.valueOf(monthlySalaryDTO.getPayableDays());
        BigDecimal daysInMonth = BigDecimal.valueOf(monthlySalaryDTO.getDaysInMonth());
        BigDecimal lossOfPayDays = BigDecimal.valueOf(monthlySalaryDTO.getLossOfPayDays());
        //calculate salary at runtime based on compensation details and days details
        Map<String, Double> earningsComponentAmountMap = new HashMap<>();
        BigDecimal totalPerDayEarnings = BigDecimal.ZERO;
        List<EmployeeSalaryComponentDTO> salaryEarningComponents = new ArrayList<>();
        {
            //Earning calculated based on per day amount and payable days
            List<@Valid EmpCompensationDtlsDTO> earningList = compensationDTO.getCompensationDetails().stream().filter(component -> component.getComponentType().equals(ComponentType.EARNINGS)).toList();

            for (EmpCompensationDtlsDTO compensationDetail : earningList) {
                BigDecimal perDaysAmount = compensationDetail.getAmount().divide(daysInMonth, 10, RoundingMode.HALF_UP);
                totalPerDayEarnings = totalPerDayEarnings.add(perDaysAmount);
                BigDecimal currentMonthEarningAmount = roundAmount(perDaysAmount.multiply(payableDays));
                //adding current month  per day amount to map for later use in deduction calculation
                earningsComponentAmountMap.put(compensationDetail.getCode(), currentMonthEarningAmount.doubleValue());
                Formula formula = null;
                if (compensationDetail.getCalculationType().equals(CalculationType.FORMULA)) {
                    EmpCompensationDtlsDTO.FormulaDTO formulaDTO = compensationDetail.getFormulaDTO();
                    Map<String, Double> vars = formulaDTO.getVars();
                    Map<String, Double> formulaSpecificVariablesOnly = new HashMap<>(vars);
                    vars.forEach((key, value) -> {
                        if (earningsComponentAmountMap.containsKey(key)) {
                            vars.put(key, earningsComponentAmountMap.get(key));
                            formulaSpecificVariablesOnly.put(key, earningsComponentAmountMap.get(key));
                        }
                    });
                    formula = Formula.builder()
                            .expression(formulaDTO.getExpression())
                            .vars(formulaSpecificVariablesOnly)
                            .viewFormula(formulaDTO.getViewFormula())
                            .build();
                    vars.putAll(earningsComponentAmountMap);
                    currentMonthEarningAmount = SalaryCalculator.evaluate(formulaDTO.getExpression(), vars);
                    currentMonthEarningAmount = roundAmount(currentMonthEarningAmount);
                } else {
                    currentMonthEarningAmount = roundAmount(currentMonthEarningAmount);
                }
                EmployeeSalaryComponentDTO employeeSalaryComponentDTO = employeeSalaryConverter.toEmployeeSalaryComponentDTO(compensationDetail);
                employeeSalaryComponentDTO.setPayableAmount(currentMonthEarningAmount);
                employeeSalaryComponentDTO.setAmountSource(SalaryComponentSource.SYSTEM_CALCULATED);
                employeeSalaryComponentDTO.setFormula(formula);
                salaryEarningComponents.add(employeeSalaryComponentDTO);
            }
            if(!ValidateUtils.isZero(attendanceSalaryLink.getNightShiftAllowanceAmt())){
                ComponentDTO nsa = componentMasterService.getByCode("NSA");
                EmployeeSalaryComponentDTO nsaComponentDTO = EmployeeSalaryComponentDTO.builder().monthlyGrossAmount(BigDecimal.ZERO)
                        .componentId(nsa.getId())
                        .componentCode(nsa.getCode())
                        .componentName(nsa.getName())
                        .componentType(ComponentType.EARNINGS)
                        .calculationType(CalculationType.FIXED)
                        .amountSource(SalaryComponentSource.SYSTEM_CALCULATED)
                        .payableAmount(BigDecimal.valueOf(attendanceSalaryLink.getNightShiftAllowanceAmt()))
                        .displayOrder(98)
                        .build();
                salaryEarningComponents.add(nsaComponentDTO);
            }
        }
        BigDecimal currentMonthGrossEarnings = salaryEarningComponents.stream().map(EmployeeSalaryComponentDTO::getPayableAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<EmployeeSalaryComponentDTO> salaryDeductionComponents = new ArrayList<>();
        {
            //Calculate Standard Deduction based on per day amount and loss of pay days
            List<@Valid EmpCompensationDtlsDTO> standardDeductionList = compensationDTO.getCompensationDetails().stream().filter(component -> component.getComponentType().equals(ComponentType.STATUTORY_DEDUCTIONS)).toList();
            BigDecimal stdDeductionTotal = BigDecimal.ZERO;
            for (EmpCompensationDtlsDTO empCompensationDtlsDTO : standardDeductionList) {
                BigDecimal currentDeductionAmount = BigDecimal.ZERO;
                if (empCompensationDtlsDTO.getCode().equals("PT")) {
                    currentDeductionAmount = ProfessionalTaxCalculator.calculateProfessionalTax(currentMonthGrossEarnings, statutoryDTO.getPayrollState(), statutoryDTO.getGender());
                } else if (empCompensationDtlsDTO.getCode().equals("ESIC")) {
                    currentDeductionAmount = ESICCalculator.calculateESI(currentMonthGrossEarnings);
                } else if (empCompensationDtlsDTO.getCode().equals("PF")) {
                    if (empCompensationDtlsDTO.getCalculationType().equals(CalculationType.FIXED)) {
                        currentDeductionAmount = empCompensationDtlsDTO.getAmount();
                    } else {
                        EmpCompensationDtlsDTO.FormulaDTO formulaDTO = empCompensationDtlsDTO.getFormulaDTO();
                        Map<String, Double> vars = formulaDTO.getVars();
                        vars.putAll(earningsComponentAmountMap);
                        currentDeductionAmount = SalaryCalculator.evaluate(formulaDTO.getExpression(), vars);
                    }
                }
                if (empCompensationDtlsDTO.getThresholdAmount() != null && currentDeductionAmount.compareTo(empCompensationDtlsDTO.getThresholdAmount()) > 0) {
                    currentDeductionAmount = empCompensationDtlsDTO.getThresholdAmount();
                }
                EmployeeSalaryComponentDTO standardDeductionsComponent = employeeSalaryConverter.toEmployeeSalaryComponentDTO(empCompensationDtlsDTO);
                standardDeductionsComponent.setPayableAmount(currentDeductionAmount);
                standardDeductionsComponent.setAmountSource(SalaryComponentSource.SYSTEM_CALCULATED);
                stdDeductionTotal = stdDeductionTotal.add(roundAmount(currentDeductionAmount));
                salaryDeductionComponents.add(standardDeductionsComponent);
            }
        }
        {
            //calculate other deduction based on per day amount and loss of pay days
            List<@Valid EmpCompensationDtlsDTO> standardDeductionList = compensationDTO.getCompensationDetails().stream().filter(component -> component.getComponentType().equals(ComponentType.DEDUCTIONS)).toList();
            BigDecimal otherDeductionTotal = BigDecimal.ZERO;
            for (EmpCompensationDtlsDTO empCompensationDtlsDTO : standardDeductionList) {
                BigDecimal currentOtherDeductionAmount = BigDecimal.ZERO;
                if (empCompensationDtlsDTO.getCalculationType().equals(CalculationType.FIXED)) {
                    if (currentMonthGrossEarnings.compareTo(BigDecimal.ZERO) > 0) {
                        currentOtherDeductionAmount = empCompensationDtlsDTO.getAmount();
                        otherDeductionTotal = otherDeductionTotal.add(currentOtherDeductionAmount);
                    }
                } else {
                    EmpCompensationDtlsDTO.FormulaDTO formulaDTO = empCompensationDtlsDTO.getFormulaDTO();
                    Map<String, Double> vars = formulaDTO.getVars();
                    vars.putAll(earningsComponentAmountMap);
                    currentOtherDeductionAmount = SalaryCalculator.evaluate(formulaDTO.getExpression(), vars);
                    otherDeductionTotal = otherDeductionTotal.add(currentOtherDeductionAmount);
                }
                EmployeeSalaryComponentDTO otherDedudctionComponent = employeeSalaryConverter.toEmployeeSalaryComponentDTO(empCompensationDtlsDTO);
                otherDedudctionComponent.setPayableAmount(currentOtherDeductionAmount);
                otherDedudctionComponent.setAmountSource(SalaryComponentSource.SYSTEM_CALCULATED);
                salaryDeductionComponents.add(otherDedudctionComponent);
            }
        }

        {
            //calculate loss of pay based on per day amount and loss of pay days
            BigDecimal lossOfPayAmount = roundAmount(totalPerDayEarnings.multiply(lossOfPayDays));
            if (lossOfPayDays.compareTo(BigDecimal.ZERO) > 0) {
                EmployeeSalaryComponentDTO lossOfPayDeductionComponent = EmployeeSalaryComponentDTO.builder()
                        .componentName("Loss of Pay")
                        .componentCode("LOP")
                        .componentType(ComponentType.DEDUCTIONS)
                        .calculationType(CalculationType.FIXED)
                        .amountSource(SalaryComponentSource.SYSTEM_CALCULATED)
                        .payableAmount(lossOfPayAmount)
                        .monthlyGrossAmount(BigDecimal.valueOf(0.00))
                        .displayOrder(99)
                        .build();
                salaryDeductionComponents.add(lossOfPayDeductionComponent);
            }
            monthlySalaryDTO.setLossOfPayAmount(lossOfPayAmount);
        }

        BigDecimal grossCurrentEarnings = salaryEarningComponents.stream().map(EmployeeSalaryComponentDTO::getPayableAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeductions = salaryDeductionComponents.stream().map(EmployeeSalaryComponentDTO::getPayableAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netPayableSalary = grossCurrentEarnings.subtract(totalDeductions);
        monthlySalaryDTO.setEarnings(salaryEarningComponents);
        monthlySalaryDTO.setDeductions(salaryDeductionComponents);
        monthlySalaryDTO.setTotalEarnings(grossCurrentEarnings);
        monthlySalaryDTO.setTotalDeductions(totalDeductions);
        monthlySalaryDTO.setNetPayableSalary(netPayableSalary);
        monthlySalaryDTO.setGrossSalary(compensationDTO.getGrossAmount());
        return monthlySalaryDTO;
    }

    public EmployeeSalaryDetailsDTO validateInComingSalary(EmployeeSalaryDetailsDTO salaryDetailsDTO,EmpCompensationDTO compensationDTO,EmployeeStatutoryDTO statutoryDTO) {
        BigDecimal daysInMonth = BigDecimal.valueOf(salaryDetailsDTO.getDaysInMonth());
        BigDecimal payableDays = BigDecimal.valueOf(salaryDetailsDTO.getPayableDays());
        BigDecimal lossOfPayDays = BigDecimal.valueOf(salaryDetailsDTO.getLossOfPayDays());
        BigDecimal totalPerDayEarnings= BigDecimal.ZERO;
        Map<String, Double> earningsComponentAmountMap = new HashMap<>();
        BigDecimal totalPayableEarnings = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        Map<String,EmpCompensationDtlsDTO > deductionComponentMap = compensationDTO.getCompensationDetails().stream().filter(component -> component.getComponentType() == ComponentType.DEDUCTIONS || component.getComponentType() == ComponentType.STATUTORY_DEDUCTIONS).collect(Collectors.toMap(
                EmpCompensationDtlsDTO::getCode,
                Function.identity()
        ));
        {
            //Earning calculated based on per day amount and payable days
            List<@Valid EmployeeSalaryComponentDTO> earningList = salaryDetailsDTO.getEarnings();

            for (EmployeeSalaryComponentDTO monthlySalaryComponent : earningList) {
                if(CalculationType.FORMULA.equals(monthlySalaryComponent.getCalculationType())){
                    Formula formula = monthlySalaryComponent.getFormula();
                    Map<String, Double> vars = formula.getVars();
                    vars.forEach((key, value) -> {
                        if (earningsComponentAmountMap.containsKey(key)) {
                            vars.put(key, earningsComponentAmountMap.get(key));
                        }
                    });
                    BigDecimal calculatedPayableAmount = SalaryCalculator.evaluate(formula.getExpression(), vars);
                    if(calculatedPayableAmount.compareTo(monthlySalaryComponent.getPayableAmount()) != 0){
                        throw new ValidationException("Payable amount mismatch for component "+ monthlySalaryComponent.getComponentCode() + " . Expected: "+ calculatedPayableAmount + " Actual: "+ monthlySalaryComponent.getPayableAmount());

                    }
                }
                earningsComponentAmountMap.put(monthlySalaryComponent.getComponentCode(), monthlySalaryComponent.getPayableAmount().doubleValue());
                totalPayableEarnings = totalPayableEarnings.add(monthlySalaryComponent.getPayableAmount());
            }
            if(totalPayableEarnings.compareTo(salaryDetailsDTO.getTotalEarnings()) != 0){
                throw new ValidationException("Total earnings mismatch. Expected: "+ totalPayableEarnings + " Actual: "+ salaryDetailsDTO.getTotalEarnings());
            }
        }
        {
            //Calculate Standard Deduction based on per day amount and loss of pay days
            List<@Valid EmployeeSalaryComponentDTO> standardDeductionList = salaryDetailsDTO.getDeductions().stream().filter(component -> component.getComponentType().equals(ComponentType.STATUTORY_DEDUCTIONS)).toList();
            for (EmployeeSalaryComponentDTO salaryComponentDTO : standardDeductionList) {
                BigDecimal currentDeductionAmount = BigDecimal.ZERO;
                if (salaryComponentDTO.getComponentCode().equals("PT")) {
                    currentDeductionAmount = ProfessionalTaxCalculator.calculateProfessionalTax(totalPayableEarnings, statutoryDTO.getPayrollState(), statutoryDTO.getGender());
                    EmpCompensationDtlsDTO pt = deductionComponentMap.get("PT");
                    if(currentDeductionAmount.compareTo(salaryComponentDTO.getPayableAmount()) != 0){
                        throw new ValidationException("Professional tax calculation mismatch. Expected: "+ currentDeductionAmount + " Actual: "+ salaryComponentDTO.getPayableAmount());
                    }
                } else if (salaryComponentDTO.getComponentCode().equals("ESIC")) {
                    currentDeductionAmount = ESICCalculator.calculateESI(totalPayableEarnings);
                    EmpCompensationDtlsDTO esic = deductionComponentMap.get("ESIC");
                    if(currentDeductionAmount.compareTo(salaryComponentDTO.getPayableAmount()) != 0){
                        throw new ValidationException("ESIC calculation mismatch. Expected: "+ currentDeductionAmount + " Actual: "+ salaryComponentDTO.getPayableAmount());
                    }
                    salaryComponentDTO.setPayableAmount(currentDeductionAmount);
                    for (EmployeeSalaryComponentDTO deduction : salaryDetailsDTO.getDeductions()) {
                        if(deduction.getComponentCode().equals("ESIC")){
                            deduction.setPayableAmount(currentDeductionAmount);
                        }
                    }
                } else if (salaryComponentDTO.getComponentCode().equals("PF")) {
                    if (salaryComponentDTO.getCalculationType().equals(CalculationType.FIXED)) {
                        currentDeductionAmount = salaryComponentDTO.getPayableAmount();
                    } else {
                        Formula formulaDTO = salaryComponentDTO.getFormula();
                        Map<String, Double> vars = formulaDTO.getVars();
                        vars.putAll(earningsComponentAmountMap);
                        currentDeductionAmount = SalaryCalculator.evaluate(formulaDTO.getExpression(), vars);
                    }
                    EmpCompensationDtlsDTO pf = deductionComponentMap.get("PF");
                    if(pf.getThresholdAmount().compareTo(currentDeductionAmount) > 0){
                        throw new ValidationException("PF amount exceded threshould amount. Expected: "+ currentDeductionAmount + " Actual: "+ salaryComponentDTO.getPayableAmount());
                    }else if(currentDeductionAmount.compareTo(salaryComponentDTO.getPayableAmount()) != 0){
                        throw new ValidationException("PF calculation mismatch. Expected: "+ currentDeductionAmount + " Actual: "+ salaryComponentDTO.getPayableAmount());
                    }
                }
                totalDeductions = totalDeductions.add(salaryComponentDTO.getPayableAmount());
            }
        }
        {
            //calculate other deduction based on per day amount and loss of pay days
            List<@Valid EmployeeSalaryComponentDTO> deductionList = salaryDetailsDTO.getDeductions().stream().filter(component -> component.getComponentType().equals(ComponentType.DEDUCTIONS) && !component.getComponentCode().equals("LOP")).toList();
            BigDecimal otherDeductionTotal = BigDecimal.ZERO;
            for (EmployeeSalaryComponentDTO empCompensationDtlsDTO : deductionList) {
                BigDecimal currentOtherDeductionAmount = BigDecimal.ZERO;
                if (empCompensationDtlsDTO.getCalculationType().equals(CalculationType.FIXED)) {
                    if (totalPayableEarnings.compareTo(BigDecimal.ZERO) > 0) {
                        currentOtherDeductionAmount = empCompensationDtlsDTO.getPayableAmount();
                    }
                } else {
                    Formula formulaDTO = empCompensationDtlsDTO.getFormula();
                    Map<String, Double> vars = formulaDTO.getVars();
                    vars.putAll(earningsComponentAmountMap);
                    BigDecimal deductionAmount = SalaryCalculator.evaluate(formulaDTO.getExpression(), vars);
                    if(deductionAmount.compareTo(empCompensationDtlsDTO.getPayableAmount()) != 0){
                        throw new ValidationException("Deduction calculation mismatch for component "+ empCompensationDtlsDTO.getComponentCode() + " . Expected: "+ deductionAmount + " Actual: "+ empCompensationDtlsDTO.getPayableAmount());
                    }
                }
                totalDeductions = totalDeductions.add(empCompensationDtlsDTO.getPayableAmount());
            }
        }
        {
            //LOP
            Optional<EmployeeSalaryComponentDTO> lossOfPayComponentOpt = salaryDetailsDTO.getDeductions().stream().filter(component -> component.getComponentCode().equals("LOP")).findFirst();
            if(lossOfPayComponentOpt.isPresent()){
                EmployeeSalaryComponentDTO salaryComponentDTO = lossOfPayComponentOpt.get();
                BigDecimal netWithoutLOP = totalPayableEarnings.subtract(totalDeductions);
                BigDecimal lossOfPayAmount = netWithoutLOP.subtract(salaryDetailsDTO.getNetPayableSalary());
                if(salaryComponentDTO.getPayableAmount().compareTo(lossOfPayAmount) != 0){
                    throw new ValidationException("Loss of Pay calculation mismatch. Expected: "+ lossOfPayAmount + " Actual: "+ salaryComponentDTO.getPayableAmount());
                }
            }
        }

        return salaryDetailsDTO;
    }
}