package com.smartcare.hrms.common.dto.master.attendance.period;

import com.smartcare.hrms.common.dto.master.employment.GradeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MonthCycleDto {

    private Long id;
    private String name;
    private String code;
    private Integer startDate;
    private Integer endDate;
    private GradeDto gradeDto;
}
