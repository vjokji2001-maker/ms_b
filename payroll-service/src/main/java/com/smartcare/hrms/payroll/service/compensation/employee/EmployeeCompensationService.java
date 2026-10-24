package com.smartcare.hrms.payroll.service.compensation.employee;

import com.smartcare.hrms.common.dto.BaseDashboardSearchDTO;
import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.globalexception.ResourceConflictException;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.payroll.config.MessageResource;
import com.smartcare.hrms.payroll.config.SalaryCalculator;
import com.smartcare.hrms.payroll.dao.EmployeeCompensationDashboardDAO;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDTO;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDtlsDTO;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmployeeCompensationDashboardDTO;
import com.smartcare.hrms.common.entity.payroll.compensation.employee.EmpCompensationDtls;
import com.smartcare.hrms.common.entity.payroll.compensation.employee.EmpCompensationHdr;
import com.smartcare.hrms.common.entity.payroll.compensation.component.ComponentMaster;
import com.smartcare.hrms.common.enumeration.CalculationType;
import com.smartcare.hrms.payroll.mappers.compensation.employee.EmpCompensationMapper;
import com.smartcare.hrms.common.repository.payroll.compensation.employee.EmpCompensationHdrRepository;
import com.smartcare.hrms.common.repository.payroll.compensation.component.ComponentMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeCompensationService {

    private @Autowired EmpCompensationHdrRepository empCompensationHdrRepository;
    private @Autowired MessageResource messageResource;
    private @Autowired EmpCompensationMapper empCompensationMapper;
    private @Autowired ComponentMasterRepository componentMasterRepository;
    private @Autowired EmployeeCompensationDashboardDAO employeeCompensationDashboardDAO;

    public PaginatedDataResponse<EmployeeCompensationDashboardDTO> getDashboardData(BaseDashboardSearchDTO baseDashboardSearchDTO) {
       return employeeCompensationDashboardDAO.getPayrollSummaryDashboard(baseDashboardSearchDTO);
    }

    @Transactional
    public void create(EmpCompensationDTO dto) {
        validateCompensationAmount(dto);
        EmpCompensationHdr entity = empCompensationMapper.toEntity(dto);
        entity.getCompensationDetails().forEach(detail -> detail.setEmpCompensationHdr(entity)); // Establish bi-directional relationship
        empCompensationHdrRepository.save(entity);
    }


    @Transactional
    public void update(String employeeId, EmpCompensationDTO dto) {
        validateCompensationAmount(dto);

        // 1. Fetch the existing managed entity
        EmpCompensationHdr existingHdr = empCompensationHdrRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceConflictException(
                        messageResource.getEnglishMessage("employee.compensation.not.found")));

        // 2. Update top-level fields (grossAmt, compTemplateId, etc.)
        empCompensationMapper.updateEntityFromDto(dto, existingHdr);

        // 3. Synchronize the Child Collection (CompensationDetails)
        List<EmpCompensationDtlsDTO> incomingDetails = dto.getCompensationDetails();

        // Remove orphans: items in DB but not in request
        List<String> incomingIds = incomingDetails.stream()
                .map(EmpCompensationDtlsDTO::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        existingHdr.getCompensationDetails().removeIf(detail -> !incomingIds.contains(detail.getId()));

        // Update existing or add new
        for (EmpCompensationDtlsDTO detailDto : incomingDetails) {
            if (detailDto.getId() != null) {
                // UPDATE existing detail row
                existingHdr.getCompensationDetails().stream()
                        .filter(existing -> existing.getId().equals(detailDto.getId()))
                        .findFirst()
                        .ifPresent(existing -> empCompensationMapper.updateDetailEntityFromDto(detailDto, existing));
            } else {
                // ADD new detail row
                EmpCompensationDtls newDetail = empCompensationMapper.toDetailEntity(detailDto);
                newDetail.setEmpCompensationHdr(existingHdr); // Establish relationship
                existingHdr.getCompensationDetails().add(newDetail);
            }
        }

        // 4. Save: Hibernate tracks changes automatically via dirty checking
        empCompensationHdrRepository.save(existingHdr);
    }


    public EmpCompensationDTO getById(String employeeId) {
        EmpCompensationHdr empCompensationHdr =
                empCompensationHdrRepository.findByEmployeeId(employeeId)
                        .orElseThrow(() -> new ResourceConflictException(
                                messageResource.getEnglishMessage("employee.compensation.not.found")));
        EmpCompensationDTO dto = empCompensationMapper.toDto(empCompensationHdr);
        dto.getCompensationDetails().sort((a, b) -> a.getSequence().compareTo(b.getSequence())); // Sort by sequence
        return dto;
    }

    public List<EmpCompensationDTO> getIdIn(List<String> employeeId) {
        List<EmpCompensationHdr> empCompensationHdr = empCompensationHdrRepository.findByEmployeeIdIn(employeeId);
        List<EmpCompensationDTO> empCompensationDTOList = List.of();
        if(!CollectionUtils.isEmpty(empCompensationHdr)){
            empCompensationDTOList = empCompensationHdr.stream().map(empCompensationMapper::toDto).collect(Collectors.toList());
            empCompensationDTOList.forEach(dto -> dto.getCompensationDetails().sort((a, b) -> a.getSequence().compareTo(b.getSequence()))); // Sort by sequence
        }
        return empCompensationDTOList;
    }


    /*
        @Method validateCompensationAmount -
        This method validates the compensation amount based on the calculation type and formula.
        It checks if the compensation head exists, and if the calculation type is FORMULA, it validates the formula and ensures that the calculated amount matches the provided amount.

     */
    private void validateCompensationAmount(EmpCompensationDTO empCompensationDTO) {
        Map<String, Double> headAmountMap = new HashMap<>();
        for (EmpCompensationDtlsDTO compensationDetailDTO : empCompensationDTO.getCompensationDetails()) {
            //check head exist or not
            ComponentMaster componentMaster = componentMasterRepository.findById(compensationDetailDTO.getComponentId())
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessageWithParam("component.not.found", new String[]{compensationDetailDTO.getComponentName()})));
            if (CalculationType.FORMULA.equals(compensationDetailDTO.getCalculationType())) {
                if (compensationDetailDTO.getFormulaDTO() == null) {
                    throw new ValidationException(messageResource.getEnglishMessageWithParam("formula.required", new String[]{compensationDetailDTO.getComponentName()}));
                } else {
                    // validate formula
                    EmpCompensationDtlsDTO.FormulaDTO formulaDTO = compensationDetailDTO.getFormulaDTO();
                    Map<String, Double> vars = formulaDTO.getVars();
                    vars.putAll(headAmountMap);
                    BigDecimal evaluate = SalaryCalculator.evaluate(formulaDTO.getExpression(), vars);
                    if (evaluate.compareTo(compensationDetailDTO.getAmount()) !=0) {
                        throw new ValidationException(messageResource.getEnglishMessageWithParam("formula.amount.mismatch", new String[]{compensationDetailDTO.getComponentName()}));
                    }
                    compensationDetailDTO.setAmount(evaluate);
                }
            }
            headAmountMap.put(compensationDetailDTO.getCode(), compensationDetailDTO.getAmount().doubleValue());
        }
        BigDecimal grossCompensation = empCompensationDTO.getCompensationDetails()
                .stream()
                .filter(a -> a.getComponentType().equals(ComponentType.EARNINGS))
                .map(EmpCompensationDtlsDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);;

        BigDecimal standardDeductions = empCompensationDTO.getCompensationDetails()
                .stream()
                .filter(a -> a.getComponentType().equals(ComponentType.STATUTORY_DEDUCTIONS))
                .map(EmpCompensationDtlsDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);;

        BigDecimal otherDeductions = empCompensationDTO.getCompensationDetails()
                .stream()
                .filter(a -> a.getComponentType().equals(ComponentType.DEDUCTIONS))
                .map(EmpCompensationDtlsDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netAmount = grossCompensation.subtract(standardDeductions).subtract(otherDeductions);
        if (grossCompensation.compareTo(empCompensationDTO.getGrossAmount()) != 0) {
            throw new ValidationException(messageResource.getEnglishMessage("gross.amount.mismatch"));
        }else if(standardDeductions.compareTo(empCompensationDTO.getStandardDeductionAmount()) != 0){
            throw new ValidationException(messageResource.getEnglishMessage("std.deduction.amount.mismatch"));
        }else if(otherDeductions.compareTo(empCompensationDTO.getOtherDeductionAmount()) != 0){
            throw new ValidationException(messageResource.getEnglishMessage("other.deduction.amount.mismatch"));
        }else if(netAmount.compareTo(empCompensationDTO.getNetAmount()) != 0){
            throw new ValidationException(messageResource.getEnglishMessage("net.amount.mismatch"));
        }
    }
}
