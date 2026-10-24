package com.smartcare.hrms.payroll.entity.finance;

import com.smartcare.hrms.payroll.dto.finance.IfscDetails;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "bank_branch")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankBranch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Many branches belong to one bank
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @Column(name = "name")
    private String name;

    @Column(name = "ifsc_code",nullable = false, unique = true)
    private String ifscCode;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    // 🔥 JSON column
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="details",columnDefinition = "json")
    private IfscDetails ifscDetails;


    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
