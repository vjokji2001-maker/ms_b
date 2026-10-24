package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffShiftRosterDTO {

    private String date; // dd/MM/yyyy
    private String dayOfWeek;
    private String shiftName;
    private String shiftCode;
    private String inTime;
    private String outTime;
    private Boolean isWeekOff;
    private Boolean isHoliday;
    private String holidayName;
    private Boolean isDefault;
}
