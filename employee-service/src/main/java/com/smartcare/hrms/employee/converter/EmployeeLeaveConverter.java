package com.smartcare.hrms.employee.converter;

import com.smartcare.hrms.employee.dto.EmployeeLeaveDtlsDTO;
import com.smartcare.hrms.employee.dto.EmployeeLeaveParentDTO;
import com.smartcare.hrms.common.entity.employee.EmployeeLeaveDtls;
import com.smartcare.hrms.common.entity.employee.EmployeeLeaveParent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeLeaveConverter {



    public static EmployeeLeaveParent toEntity(EmployeeLeaveParentDTO dto, EmployeeLeaveParent parentEntity) {
        if (dto == null) return null;

        EmployeeLeaveParent entity = parentEntity != null ? parentEntity : EmployeeLeaveParent.builder().build();

        // Map basic fields
        entity.setBalance(dto.getBalance());
        if (entity.getEmployeeLeaveDtls() == null) {
            entity.setEmployeeLeaveDtls(new ArrayList<>());
        } else {
            entity.getEmployeeLeaveDtls().clear(); // safe now
        }
        // Map child details
        if (dto.getEmployeeLeaveDtlsDTO() != null) {
            List<EmployeeLeaveDtls> dtlsList = dto.getEmployeeLeaveDtlsDTO().stream()
                    .map(dtlsDto -> toEntity(dtlsDto, entity))
                    .collect(Collectors.toList());
            entity.getEmployeeLeaveDtls().addAll(dtlsList);
        }

        return entity;
    }

    // Convert Child DTO to Entity
    public static EmployeeLeaveDtls toEntity(EmployeeLeaveDtlsDTO dto, EmployeeLeaveParent parent) {
        if (dto == null) return null;

        return EmployeeLeaveDtls.builder()
                .leaveNameId(dto.getLeaveNameId())
                .yearlyCount(dto.getYearlyCount())
                .quaterlyCount(dto.getQuaterlyCount())
                .monthlyCount(dto.getMonthlyCount())
                .pastDays(dto.getPastDays())
                .futureDays(dto.getFutureDays())
                .halfDayAllowed(dto.isHalfDayAllowed())
                .employeeLeave(parent)
                .build();
    }


}
