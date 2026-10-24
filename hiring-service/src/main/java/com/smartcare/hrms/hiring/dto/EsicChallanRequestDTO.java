package com.smartcare.hrms.hiring.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class EsicChallanRequestDTO {

    private String financialYear;

    private String month;

    private String searchEmployee;

    private Integer page = 0;

    private Integer size = 10;
}