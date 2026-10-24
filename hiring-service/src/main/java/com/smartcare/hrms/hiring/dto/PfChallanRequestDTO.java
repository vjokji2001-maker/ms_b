package com.smartcare.hrms.hiring.dto;

import lombok.Data;

@Data
public class PfChallanRequestDTO {

    private String financialYear;

    private String month;

    private String searchEmployee;

    private Integer page = 0;

    private Integer size = 10;
}