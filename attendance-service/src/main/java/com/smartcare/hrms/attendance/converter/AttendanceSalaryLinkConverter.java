package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.attendance.dto.attendance.AttendanceSalaryLinkDTO;
import com.smartcare.hrms.common.entity.attendance.AttendanceSalaryLink;
import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import com.smartcare.hrms.common.enumeration.PayrollStatus;
import com.smartcare.hrms.common.utility.EnumConverter;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Month;

@Component
public class AttendanceSalaryLinkConverter {

    public AttendanceSalaryLink convertDtoToEntity (AttendanceSalaryLinkDTO dto){
        if(dto==null) return null;

        String monthYear= dto.getMonthYear();
        String formattedYearMonth=null;
        if(monthYear!=null){
            String parts[]=monthYear.split("-");
            int year= Integer.parseInt(parts[0]);
            int month= Month.valueOf(parts[1].toUpperCase()).getValue();

            formattedYearMonth=year+"-"+String.format("%02d",month);

        }

        AttendanceSalaryLink attendanceSalaryLink= AttendanceSalaryLink.builder()
                .employeeId(dto.getEmployeeId())
                .monthYear(formattedYearMonth)
                .attendanceStatus(AttendanceStatusEnum.PENDING)
                .payrollStatus(PayrollStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .nightShiftExist(false)
                .build();
        return attendanceSalaryLink;

    }
}
