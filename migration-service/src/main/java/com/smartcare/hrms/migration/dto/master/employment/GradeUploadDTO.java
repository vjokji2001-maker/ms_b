package com.smartcare.hrms.migration.dto.master.employment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GradeUploadDTO {

    private String name;
    private String code;
}
