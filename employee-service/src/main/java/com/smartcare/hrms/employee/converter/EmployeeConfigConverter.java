package com.smartcare.hrms.employee.converter;

import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.employee.EmployeeConfig;
import com.smartcare.hrms.common.entity.master.attendance.approvalgroup.ApprovalGroup;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;
import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffParent;
import com.smartcare.hrms.employee.dto.EmployeeConfigDTO;

import java.util.UUID;

public class EmployeeConfigConverter {


    public static EmployeeConfig convertForUpdateForSingleHoliday(EmployeeConfig entity, EmployeeConfigDTO dto) {

        if (dto == null || entity == null) return entity;

        // Employee
        if (dto.getEmployeeId() != null && !dto.getEmployeeId().isEmpty()) {
            Employee emp = new Employee();
            emp.setId(UUID.fromString(dto.getEmployeeId()));
            entity.setEmployee(emp);
        }

        // HolidayParent
        if (dto.getHolidayId() != null && dto.getHolidayId() > 0) {
            HolidayParent holidayParent = new HolidayParent();
            holidayParent.setId(dto.getHolidayId());
            entity.setHolidayParent(holidayParent);
        }

        return entity;
    }


    public static EmployeeConfig convertForUpdateForSingleWeekOff(EmployeeConfig entity, EmployeeConfigDTO dto) {

        if (dto == null || entity == null) return entity;

        // Employee
        if (dto.getEmployeeId() != null && !dto.getEmployeeId().isEmpty()) {
            Employee emp = new Employee();
            emp.setId(UUID.fromString(dto.getEmployeeId()));
            entity.setEmployee(emp);
        }

        // WeekOff
        if (dto.getWeekOffId() != null && dto.getWeekOffId() > 0) {
            WeekOffParent weekOff = new WeekOffParent();
            weekOff.setId(dto.getWeekOffId());
            entity.setWeekOffId(weekOff);
        }

        return entity;
    }


    public static EmployeeConfig convertForUpdateSingleApprovalGroup(EmployeeConfig entity, EmployeeConfigDTO dto) {

        if (dto == null || entity == null) return entity;

        // Employee
        Employee emp = new Employee();
        emp.setId(UUID.fromString(dto.getEmployeeId()));
        entity.setEmployee(emp);

        // Approval group
        ApprovalGroup approvalGroup = new ApprovalGroup();
        approvalGroup.setId(UUID.fromString(dto.getApprovalId()));
        entity.setApprovalId(approvalGroup);

        return entity;
    }
}
