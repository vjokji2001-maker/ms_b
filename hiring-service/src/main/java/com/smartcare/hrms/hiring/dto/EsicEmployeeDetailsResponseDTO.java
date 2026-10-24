package com.smartcare.hrms.hiring.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsicEmployeeDetailsResponseDTO {

    private EsicEmployeeSummaryDTO summary;

    private PageResponse<EsicEmployeeDetailsDTO> employees;
}