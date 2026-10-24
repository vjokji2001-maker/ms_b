package com.smartcare.hrms.hiring.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "esic_challan_employee")
@Data
public class EsicChallanEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challan_id", nullable = false)
    private Long challanId;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "esic_number")
    private String esicNumber;

    @Column(name = "department")
    private String department;

    @Column(name = "wages", precision = 15, scale = 2)
    private BigDecimal wages;

    @Column(name = "employee_share", precision = 15, scale = 2)
    private BigDecimal employeeShare;

    @Column(name = "employer_share", precision = 15, scale = 2)
    private BigDecimal employerShare;

    @Column(name = "total", precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "status")
    private String status;
}