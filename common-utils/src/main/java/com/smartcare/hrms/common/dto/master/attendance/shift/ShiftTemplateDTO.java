package com.smartcare.hrms.common.dto.master.attendance.shift;

import com.smartcare.hrms.common.dto.BaseIdDTO;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ShiftTemplateDTO extends BaseIdDTO {
    private String name;
    private String code;
    private boolean nightShift;
    private boolean halfDay;
    private boolean hourBased;
    private String inTime;
    private String outTime;
    private String totalHours;
    private String firstHalfOutTime;
    private String secondHalfInTime;
    private String totalHalfDayHours;
    private int bufferTime;
    private int cutOffTime;
    private List<ShiftLateMarkRuleDTO> lateMarkRuleDTOList;
    private Double halfRate;
    private Double fullRate;
}
