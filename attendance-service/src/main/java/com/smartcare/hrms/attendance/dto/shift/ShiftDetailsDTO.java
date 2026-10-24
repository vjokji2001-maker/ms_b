package com.smartcare.hrms.attendance.dto.shift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftDetailsDTO {

    private String shiftId;
    private String shiftCode;
    private String shiftName;
    private String date;
}
