package com.smartcare.hrms.migration.dto.attendance;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceUploadDTO {
    @ExcelProperty("Employee Code")
    private String empCode;            // user enters emp code, NOT emp id

    @ExcelProperty("Attendance Date")
    private String attendanceDate;     // yyyy-MM-dd

    @ExcelProperty("In Time")
    private String inTime;             // HH:mm:ss  (nullable)

    @ExcelProperty("Out Time")
    private String outTime;            // HH:mm:ss  (nullable)





}

