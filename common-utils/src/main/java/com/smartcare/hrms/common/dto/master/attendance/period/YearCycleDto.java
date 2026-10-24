package com.smartcare.hrms.common.dto.master.attendance.period;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class YearCycleDto {

    private Long id;
    private String name;
    private String code;
    private String fromMonthYear;
    private String toMonthYear;
}
