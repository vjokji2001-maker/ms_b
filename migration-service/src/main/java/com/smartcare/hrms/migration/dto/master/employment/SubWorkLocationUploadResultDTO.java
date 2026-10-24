package com.smartcare.hrms.migration.dto.master.employment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubWorkLocationUploadResultDTO {

    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<SubWorkLocationUploadErrorDTO> errors;
}
