package com.smartcare.hrms.attendance.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceSalaryPayloadDTO {

    private String  attendanceSalaryLinkId;
    private Double payableDays;
    private Double lossOfPayDays;

}
