package com.smartcare.hrms.attendance.dto.application.swipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EmployeeShiftDetailDTO {

    private Long empShiftId;
    private Long shiftTemplateId;
    private String shiftName;
    private String shiftCode;
    private String shiftInTime;
    private String shiftOutTime;
    private String firstHalfOutTime;
    private String secondHalfInTime;
    private String empShiftDate;
}
