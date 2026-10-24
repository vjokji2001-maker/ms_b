package com.smartcare.hrms.attendance.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftRuleDTO {

    private Long lateInFromMin;
    private Long lateInToMin;
    private Float lateInMarkCount;

    private Long earlyOutFromMin;
    private Long earlyOutToMin;
    private Float earlyOutMarkCount;
}
