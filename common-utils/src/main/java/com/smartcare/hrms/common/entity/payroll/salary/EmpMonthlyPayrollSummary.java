package com.smartcare.hrms.common.entity.payroll.salary;

import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDTO;
import com.smartcare.hrms.common.entity.BaseEntityUUID;
import com.smartcare.hrms.common.entity.attendance.AttendanceSalaryLink;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "emp_monthly_payroll_summary")
public class EmpMonthlyPayrollSummary extends BaseEntityUUID {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_salary_link_id")
    private AttendanceSalaryLink attendanceSalaryLink;

    @Column(name= "payable_days")
    private Double payableDays;

    @Column(name= "lop_days")
    private Double lossOfPayDays;

    @Column(name = "ttl_earnings")
    private BigDecimal totalEarnings;

    @Column(name = "ttl_std_deductions")
    private BigDecimal totalStandardDeductions;

    @Column(name = "ttl_othr_deductions")
    private BigDecimal totalOtherDeductions;

    @Column(name = "ttl_payable_earnings")
    private BigDecimal totalPayableEarnings;

    @Column(name = "ttl_payable_std_deductions")
    private BigDecimal totalPayableStandardDeductions;

    @Column(name = "ttl_payable_othr_deductions")
    private BigDecimal totalPayableOtherDeductions;

    @Column(name = "net_payable")
    private BigDecimal netPayable;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "emp_compensation", columnDefinition = "longtext")
    private EmpCompensationDTO empCompensationHdr;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "salary_dtls", columnDefinition = "longtext")
    private List<EmpSalaryComponentDetails> salaryComponents;

}
