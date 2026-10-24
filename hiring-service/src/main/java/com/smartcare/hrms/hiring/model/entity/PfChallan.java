package com.smartcare.hrms.hiring.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "pf_challan",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pf_challan_financial_month",
                        columnNames = {"financial_year", "month_year"}
                )
        }
)
@Data
public class PfChallan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challan_number", nullable = false, unique = true)
    private String challanNumber;

    @Column(name = "financial_year", nullable = false)
    private String financialYear;

    @Column(name = "month", nullable = false)
    private String month;

    @Column(name = "month_year", nullable = false, length = 7)
    private String monthYear;

    @Column(name = "total_employees", nullable = false)
    private Integer totalEmployees;

    @Column(name = "employee_pf", precision = 15, scale = 2, nullable = false)
    private BigDecimal employeePF;

    @Column(name = "employer_pf", precision = 15, scale = 2, nullable = false)
    private BigDecimal employerPF;

    @Column(name = "eps", precision = 15, scale = 2, nullable = false)
    private BigDecimal eps;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "generated_on", nullable = false)
    private LocalDate generatedOn;
}