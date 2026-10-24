package com.smartcare.hrms.payroll.service.finance;

import com.smartcare.hrms.payroll.dto.finance.BankDTO;
import com.smartcare.hrms.payroll.entity.finance.Bank;
import com.smartcare.hrms.payroll.mappers.finance.BankMapper;
import com.smartcare.hrms.payroll.repository.finance.BankRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankRepository bankRepository;
    private final BankMapper bankMapper;

    // =========================
    // CREATE
    // =========================
    public BankDTO createBank(BankDTO dto) {
        Bank bank = bankMapper.toBankEntity(dto);
        return bankMapper.toBankDTO(bankRepository.save(bank));
    }

    // =========================
    // READ
    // =========================
    public BankDTO getBankById(Long id) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bank not found"));
        return bankMapper.toBankDTO(bank);
    }

    public List<BankDTO> getAllBanks() {
        return bankRepository.findAll()
                .stream()
                .map(bankMapper::toBankDTO)
                .toList();
    }

    // =========================
    // UPDATE
    // =========================
    public BankDTO updateBank(Long id, BankDTO dto) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bank not found"));

        bankMapper.updateBankFromDto(dto, bank);
        return bankMapper.toBankDTO(bankRepository.save(bank));
    }

    // =========================
    // DELETE (Soft)
    // =========================
    public void deactivateBank(Long id) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bank not found"));

        bank.setIsActive(false);
        bankRepository.save(bank);
    }
}