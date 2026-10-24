package com.smartcare.hrms.payroll.service.finance;

import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.payroll.config.MessageResource;
import com.smartcare.hrms.payroll.dto.finance.EmployeeBankAccountDTO;
import com.smartcare.hrms.payroll.dto.finance.IfscDetails;
import com.smartcare.hrms.payroll.entity.finance.Bank;
import com.smartcare.hrms.payroll.entity.finance.BankBranch;
import com.smartcare.hrms.payroll.entity.finance.EmployeeBankAccount;
import com.smartcare.hrms.payroll.mappers.finance.EmployeeBankAccountMapper;
import com.smartcare.hrms.payroll.repository.finance.BankBranchRepository;
import com.smartcare.hrms.payroll.repository.finance.BankRepository;
import com.smartcare.hrms.payroll.repository.finance.EmployeeBankAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeBankAccountService {

    private @Autowired EmployeeBankAccountMapper employeeBankAccountMapper;
    private @Autowired EmployeeBankAccountRepository employeeBankAccountRepository;
    private @Autowired EmployeeRepository employeeRepository;
    private @Autowired MessageResource messageResource;
    private @Autowired BankRepository bankRepository;
    private @Autowired BankBranchRepository bankBranchRepository;
    private @Autowired IfscService ifscService;


    @Transactional
    public void createEmployeeBankAccount(EmployeeBankAccountDTO dto) {

        if (!employeeRepository.existsById(UUID.fromString(dto.getEmpId()))) {
            throw new ValidationException(messageResource.getEnglishMessage("emp.not.found"));
        }
        if (dto.getIfscCode() != null && !dto.getIfscCode().isBlank()) {
            Long branchId = resolveBranchIdByIfsc(dto.getIfscCode().toUpperCase(), dto.getBankId());
            dto.setBranchId(branchId);
        } else if (dto.getBranchId() != null && !bankBranchRepository.existsById(dto.getBranchId())) {
            throw new ValidationException(messageResource.getEnglishMessage("bank.branch.not.found"));
        }

        EmployeeBankAccount employeeBankAccount = employeeBankAccountMapper.toEntity(dto);

        employeeBankAccountRepository.save(employeeBankAccount);
    }

    public EmployeeBankAccountDTO getById(String id) {

        EmployeeBankAccount account = employeeBankAccountRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Employee Bank details not found"));

        EmployeeBankAccountDTO dto = employeeBankAccountMapper.toDTO(account);

        if (account.getBranchId() != null) {
            bankBranchRepository.findById(account.getBranchId())
                    .ifPresent(branch -> dto.setIfscCode(branch.getIfscCode()));
        }

        return dto;
    }

    public List<EmployeeBankAccountDTO> getAllEmployeeBankAccount() {

        return employeeBankAccountRepository.findAll()
                .stream()
                .map(employeeBankAccountMapper::toDTO)
                .toList();
    }

    @Transactional
    public EmployeeBankAccountDTO update(EmployeeBankAccountDTO dto) {

        EmployeeBankAccount account = employeeBankAccountRepository.findById(UUID.fromString(dto.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Employee Bank details not found"));

        if (dto.getBankId() != null && !bankRepository.existsById(dto.getBankId())) {
            throw new ValidationException(messageResource.getEnglishMessage("bank.details.not.found"));
        }

        if (dto.getBranchId() != null && !bankBranchRepository.existsById(dto.getBranchId())) {
            throw new ValidationException(messageResource.getEnglishMessage("bank.branch.not.found"));
        }

        employeeBankAccountMapper.updateEntityFromDTO(dto, account);

        return employeeBankAccountMapper.toDTO(employeeBankAccountRepository.save(account));
    }

    @Transactional
    public void delete(String id) {

        EmployeeBankAccount account = employeeBankAccountRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Employee Bank details not found"));

        account.setIsActive(false);
        employeeBankAccountRepository.save(account);
    }

    private Long resolveBranchIdByIfsc(String ifscCode, Long bankId) {
        // Check DB first
        Optional<BankBranch> existing = bankBranchRepository.findByIfscCode(ifscCode);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        // Not in DB → fetch from Razorpay
        IfscDetails ifscDetails = ifscService.fetchIfscDetails(ifscCode);

        if (ifscDetails == null) {
            throw new ValidationException("Invalid IFSC code" );
        }

        // Find bank by code from Razorpay response
        Bank bank = bankRepository.findByCode(ifscDetails.getBankCode())
                .orElseGet(() -> {
                    return bankRepository.save(Bank.builder()
                            .name(ifscDetails.getBank())
                            .shortName(ifscDetails.getBankCode())
                            .code(ifscDetails.getBankCode())
                            .isActive(true)
                            .build());
                });

        // Save branch to DB
        BankBranch newBranch = BankBranch.builder()
                .ifscCode(ifscCode)
                .name(ifscDetails.getBranch())
                .city(ifscDetails.getCity())
                .state(ifscDetails.getState())
                .ifscDetails(ifscDetails)
                .bank(bank)
                .isActive(true)
                .build();

        BankBranch saved = bankBranchRepository.save(newBranch);

        return saved.getId();
    }
}
