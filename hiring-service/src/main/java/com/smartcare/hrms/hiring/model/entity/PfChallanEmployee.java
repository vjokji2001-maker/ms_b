package com.smartcare.hrms.hiring.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "pf_challan_employee")
@Data
public class PfChallanEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challan_id", nullable = false)
    private Long challanId;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "designation")
    private String designation;

    @Column(name = "uan")
    private String uan;

    @Column(name = "pf_number")
    private String pfNumber;

    @Column(name = "department")
    private String department;

    @Column(name = "basic_da", precision = 15, scale = 2)
    private BigDecimal basicDA;

    @Column(name = "employee_pf", precision = 15, scale = 2)
    private BigDecimal employeePF;

    @Column(name = "employer_pf", precision = 15, scale = 2)
    private BigDecimal employerPF;

    @Column(name = "eps", precision = 15, scale = 2)
    private BigDecimal eps;

    @Column(name = "total", precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "status")
    private String status;
}