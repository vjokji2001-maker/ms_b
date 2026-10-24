package com.smartcare.hrms.common.entity.payroll.compensation.employee;

import com.smartcare.hrms.common.entity.BaseEntityUUID;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "emp_compensation_hdr")
public class EmpCompensationHdr extends BaseEntityUUID {


    @Column(name = "emp_id",nullable = false)
    private String empId;

    @Column(name = "comp_tmplt_id", nullable = false)
    private Long compTemplateId;

    @Column(name = "gross_amt", nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "std_deduction_amt", nullable = false)
    private BigDecimal standardDeductionAmount;

    @Column(name = "other_deduction_amt", nullable = false)
    private BigDecimal otherDeductionAmount;

    @Column(name = "net_amt", nullable = false)
    private BigDecimal netAmount;

    @OneToMany(
            mappedBy = "empCompensationHdr",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<EmpCompensationDtls> compensationDetails;
}
