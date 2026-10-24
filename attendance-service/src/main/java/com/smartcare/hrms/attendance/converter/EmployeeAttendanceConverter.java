package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.attendance.dto.attendance.EmpAttendanceDTO;
import com.smartcare.hrms.common.entity.attendance.EmployeeAttendance;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmployeeAttendanceConverter {


    public EmployeeAttendance toEntity(EmpAttendanceDTO dto, Long existingId) {
        if (dto == null) return null;

        EmployeeAttendance entity = EmployeeAttendance.builder()
                .employeeId(dto.getEmployeeId())
                .employeeShiftId(dto.getEmpShiftId())
                .attendanceDate(dto.getAttendanceDate())
                .inTime(dto.getInTime())
                .outTime(dto.getOutTime())
                .dayType(dto.getDayType())
                .dayType1(dto.getDayType1())
                .portion(dto.getPortion())
                .lateInMark(dto.getLateInMark())
                .earlyOutMark(dto.getEarlyOutMark())
                .status(dto.getStatus())
                .build();


        entity.setId(existingId);

        return entity;
    }

    // Overload — INSERT only (existingId = null)
    public EmployeeAttendance toEntity(EmpAttendanceDTO dto) {
        return toEntity(dto, null);
    }

    public List<EmployeeAttendance> toEntityList(List<EmpAttendanceDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return Collections.emptyList();
        return dtoList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
