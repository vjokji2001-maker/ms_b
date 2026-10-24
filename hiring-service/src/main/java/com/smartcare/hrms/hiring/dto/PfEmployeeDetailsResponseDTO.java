package com.smartcare.hrms.hiring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PfEmployeeDetailsResponseDTO {

    private PfEmployeeSummaryDTO summary;

    private PageResponse<PfEmployeeDetailsDTO> employees;
}