package com.smartcare.hrms.payroll.mappers.finance;


import com.smartcare.hrms.payroll.dto.finance.BankBranchDTO;
import com.smartcare.hrms.payroll.dto.finance.BankDTO;
import com.smartcare.hrms.payroll.entity.finance.Bank;
import com.smartcare.hrms.payroll.entity.finance.BankBranch;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BankMapper {

    // =====================================================
    // 🟢 BANK MAPPING
    // =====================================================

    BankDTO toBankDTO(Bank entity);

    Bank toBankEntity(BankDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBankFromDto(BankDTO dto, @MappingTarget Bank entity);


    // =====================================================
    // 🔵 BANK BRANCH MAPPING
    // =====================================================

    // Entity → DTO
    @Mapping(source = "bank.id", target = "bankId")
    @Mapping(source = "bank.name", target = "bankName")
    @Mapping(target = "isActive", constant = "true")
    BankBranchDTO toBankBranchDTO(BankBranch entity);


    // Request → Entity
    @Mapping(source = "bankId", target = "bank")
    BankBranch toBankBranchEntity(BankBranchDTO request);


    // Update existing entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "bankId", target = "bank")
    void updateBankBranchFromRequest(BankBranchDTO request,
                                     @MappingTarget BankBranch entity);


    // =====================================================
    // 🔥 COMMON / HELPER METHODS
    // =====================================================

    // Convert bankId → Bank entity
    default Bank map(Long bankId) {
        if (bankId == null) return null;
        Bank bank = new Bank();
        bank.setId(bankId);
        return bank;
    }
}
