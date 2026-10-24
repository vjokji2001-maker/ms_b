package com.smartcare.hrms.payroll.entity.finance;

import com.smartcare.hrms.common.entity.BaseEntityUUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_bank_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeBankAccount extends BaseEntityUUID {

    @Column(name = "emp_id")
    private String empId;

    @Column(name = "bank_id")
    private Long bankId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "account_holder_name", length = 150)
    private String accountHolderName;

    @Column(name = "account_type", length = 20)
    private String accountType; // SAVINGS, CURRENT, SALARY

    @Column(name = "is_primary")
    private Boolean isPrimary = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ⏱ Auto timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
