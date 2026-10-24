package com.smartcare.hrms.common.dto.master.attendance.shift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ShiftLateMarkRuleDTO {
    private Long id;
    private int fromTime;
    private int toTime;
    private double markCount;
    private double totalMarkCount;
    private String type;
}
