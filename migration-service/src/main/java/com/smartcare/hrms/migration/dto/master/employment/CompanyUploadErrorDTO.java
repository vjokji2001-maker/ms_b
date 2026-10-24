package com.smartcare.hrms.migration.dto.master.employment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyUploadErrorDTO {

    private int rowNumber;
    private String name;
    private String code;
    private String countryName;
    private String stateName;
    private String cityName;
    private String designationCode;
    private String reason;
}
