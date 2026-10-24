package com.smartcare.hrms.attendance.dto.shift;

import com.smartcare.hrms.attendance.dto.attendance.ShiftRuleDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeShiftDTO {

    private Long id;
    private String empId;
    private Long shiftId;
    private String shiftName;
    private String shiftCode;
    private Long empShiftId;
    private String shiftInTime;
    private String empInTime;
    private String shiftOutTime;
    private String empOutTime;
    private String firstHalfOutTime;
    private String secondHalfInTime;
    private String date;
    private Boolean isDefault;

    private BigDecimal cutoffHr;
    private Integer graceTimeMins;
    private LocalTime autoAdjustTime;
    private Integer endBefore;
    private Boolean halfDayAllowed;

    private String lateInFromMin;
    private String lateInToMin;
    private float  lateInMarkCount;

    private String earlyOutFromMin;
    private String earlyOutToMin;
    private float  earlyOutMarkCount;

    private Double halfRate;
    private Double fullRate;
    private Boolean nightShiftExist;
    private Double nighShiftAmount;
    private List<ShiftRuleDTO> lateRulesSlab;
}
