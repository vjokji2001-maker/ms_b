package com.smartcare.hrms.attendance.dto.shift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeShiftDashboardDTO {

    private String employeeId;
    private String employeeName;
    private String employeeCode;
    private List<ShiftDetailsDTO> empShiftList;
}
