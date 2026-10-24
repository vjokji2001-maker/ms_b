package com.smartcare.hrms.payroll.service.finance;


import com.smartcare.hrms.payroll.dto.finance.BankBranchDTO;
import com.smartcare.hrms.payroll.dto.finance.IfscDetails;
import com.smartcare.hrms.payroll.entity.finance.Bank;
import com.smartcare.hrms.payroll.entity.finance.BankBranch;
import com.smartcare.hrms.payroll.mappers.finance.BankMapper;
import com.smartcare.hrms.payroll.repository.finance.BankBranchRepository;
import com.smartcare.hrms.payroll.repository.finance.BankRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankBranchService {

    private final BankBranchRepository bankBranchRepository;
    private final BankRepository bankRepository;
    private final BankMapper bankMapper;
    private final IfscService ifscService;

    // =========================
    // CREATE
    // =========================
    public BankBranchDTO createBankBranch(BankBranchDTO request) {

        // 🔥 Normalize IFSC
        String ifsc = request.getIfscCode().trim().toUpperCase();

        // 🔥 Check duplicate
        bankBranchRepository.findByIfscCode(ifsc)
                .ifPresent(b -> {
                    throw new RuntimeException("IFSC already exists");
                });

        // 🔥 Fetch IFSC details
        IfscDetails ifscDetails = ifscService.fetchIfscDetails(ifsc);

        // 🔥 Validate bank
        Bank bank = bankRepository.findById(request.getBankId())
                .orElseThrow(() -> new EntityNotFoundException("Bank not found"));

        BankBranch branch = bankMapper.toBankBranchEntity(request);

        branch.setIfscCode(ifsc);
        branch.setBank(bank);

        // 🔥 Auto-fill from IFSC API
        branch.setName(ifscDetails.getBranch());
        branch.setCity(ifscDetails.getCity());
        branch.setState(ifscDetails.getState());
        branch.setIfscDetails(ifscDetails);

        return bankMapper.toBankBranchDTO(bankBranchRepository.save(branch));
    }

    // =========================
    // READ
    // =========================
    public BankBranchDTO getBranchById(Long id) {
        BankBranch branch = bankBranchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        return bankMapper.toBankBranchDTO(branch);
    }

    public BankBranchDTO getBranchByIfsc(String ifscCode) {
        BankBranch branch = bankBranchRepository.findByIfscCode(ifscCode.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        return bankMapper.toBankBranchDTO(branch);
    }

    public List<BankBranchDTO> getBranchesByBank(Long bankId) {
        return bankBranchRepository.findByBankId(bankId)
                .stream()
                .map(bankMapper::toBankBranchDTO)
                .toList();
    }

    public List<BankBranchDTO> getAllBranches() {
        return bankBranchRepository.findAll()
                .stream()
                .map(bankMapper::toBankBranchDTO)
                .toList();
    }

    // =========================
    // UPDATE
    // =========================
    public BankBranchDTO updateBankBranch(Long id, BankBranchDTO request) {

        BankBranch branch = bankBranchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        if (request.getBankId() != null) {
            Bank bank = bankRepository.findById(request.getBankId())
                    .orElseThrow(() -> new EntityNotFoundException("Bank not found"));
            branch.setBank(bank);
        }

        bankMapper.updateBankBranchFromRequest(request, branch);

        return bankMapper.toBankBranchDTO(bankBranchRepository.save(branch));
    }

    // =========================
    // DELETE (Soft)
    // =========================
    public void deactivateBankBranch(Long id) {
        BankBranch branch = bankBranchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        branch.setIsActive(false);
        bankBranchRepository.save(branch);
    }
}
