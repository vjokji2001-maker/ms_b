package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.common.bean.attendance.AttendanceDetailsBean;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeDailyPunchLogDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeSwipeRequestDTO;
import com.smartcare.hrms.attendance.dto.attendance.AttendanceDetailsDTO;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDTO;
import com.smartcare.hrms.common.enumeration.ApplicationType;
import com.smartcare.hrms.common.utility.DateHelper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class AttendanceDetailsConverter {
    public AttendanceDetailsDTO convertBeanToDTO(List<AttendanceDetailsBean> beanList) {
        if (beanList == null || beanList.isEmpty()) {
            return null;
        }


        AttendanceDetailsBean first = beanList.get(0);

        List<EmployeeShiftDTO> shifts = new ArrayList<>();
        shifts.add(EmployeeShiftDTO.builder()
                .shiftCode(first.getShiftCode())
                .shiftName(first.getShiftName())
                .shiftInTime(DateHelper.formatLocalTime(first.getShiftInTime()))
                .shiftOutTime(DateHelper.formatLocalTime(first.getShiftOutTime()))
                .date(DateHelper.formatDateObjectToUserViewDate(first.getShiftDate()))
                .empInTime(DateHelper.formatLocalTime(first.getEmpInTime()))
                .empOutTime(DateHelper.formatLocalTime(first.getEmpOutTime()))
                .build());

        List<LeaveODRequestDTO> leaves = new ArrayList<>();
        List<LeaveODRequestDTO> ods = new ArrayList<>();
//        for (AttendanceDetailsBean b : beanList) {
//            if (b.getLeaveName() != null && leaves.stream().noneMatch(e -> e.getApplicationType().equals(b.getAppType()) && e.getToDateStr().equals(DateHelper.formatDateObjectToUserViewDate(b.getLeaveDate())))) {
//                leaves.add(LeaveODRequestDTO.builder()
//                        .leaveName(b.getLeaveName())
//                        .applicationType(b.getAppType())
//                        .toDateStr(DateHelper.formatDateObjectToUserViewDate(b.getLeaveDate()))
//                        .reason(b.getLeaveReason())
//                        .build());
//            }
//        }
        for (AttendanceDetailsBean b : beanList) {
            if (b.getAppType() == null || b.getLeaveDate() == null)
                continue;

            if ("LEAVE_RQST".equalsIgnoreCase(b.getAppType()) ) {
                if (leaves.stream().noneMatch(e -> Objects.equals(e.getLeaveDate(), b.getLeaveDate()))) {
                    leaves.add(LeaveODRequestDTO.builder()
                            .leaveName(b.getLeaveName())
                            .applicationType(String.valueOf(ApplicationType.valueOf(b.getAppType())))
                            .leaveDate(b.getLeaveDate())
                            .leaveType(String.valueOf(b.getLeaveType()))
                            .reason(b.getLeaveReason())
                            .status(String.valueOf(b.getStatus()))
                            .build());
                }
            }
            else if ("OD_RQST".equalsIgnoreCase(b.getAppType()) ) {
                if (ods.stream().noneMatch(e -> Objects.equals(e.getLeaveDate(), b.getLeaveDate()))) {
                    ods.add(LeaveODRequestDTO.builder()
                            .leaveName(b.getLeaveName())
                            .applicationType(String.valueOf(ApplicationType.valueOf(b.getAppType())))
                            .leaveDate(b.getLeaveDate())
                            .leaveType(String.valueOf(b.getLeaveType()))
                            .reason(b.getLeaveReason())
                            .status(String.valueOf(b.getStatus()))
                            .build());
                }
            }
        }

        List<EmployeeSwipeRequestDTO> swipes = new ArrayList<>();
        for (AttendanceDetailsBean b : beanList) {
            if (b.getSwipeDate() != null && swipes.stream().noneMatch(e -> e.getSwipeDate().equals(DateHelper.formatDateObjectToUserViewDate(b.getSwipeDate())))) {
                swipes.add(EmployeeSwipeRequestDTO.builder()
                        .swipeDate(DateHelper.formatDateObjectToUserViewDate(b.getSwipeDate()))
                        .reqInTime(DateHelper.formatLocalTime(b.getSwipeRqstInTime()))
                        .reqOutTime(DateHelper.formatLocalTime(b.getSwipeRqstOutTime()))
                        .remark(b.getSwipeRemark())
                        .build());
            }
        }

        List<EmployeeDailyPunchLogDTO> punchLogs = new ArrayList<>();
        for (AttendanceDetailsBean b : beanList) {
            if (b.getPunchTime() != null && punchLogs.stream().noneMatch(e -> e.getPunchDateTime().equals(DateHelper.formatDateTimeObjectToUserDateView(b.getPunchTime())))) {
                punchLogs.add(EmployeeDailyPunchLogDTO.builder()
                        .punchDateTime(DateHelper.formatDateTimeObjectToUserDateView(b.getPunchTime()))
                        .punchDate(DateHelper.formatDateObjectToUserViewDate(b.getPunchTime().toLocalDate()))
                        .punchType(b.getPunchType())
                        .punchDirection(b.getDirection())
                        .build());
            }
        }

        return AttendanceDetailsDTO.builder()
                .empName(first.getEmpName())
                .empCode(first.getEmpCode())
                .shifts(shifts)
                .leaves(leaves)
                .ods(ods)
                .swipes(swipes)
                .punchLogs(punchLogs)
                .build();
    }
}
