package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestApproverDetailsBean;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class CancelODRequestApproverConverter {
    public LeaveApproverDTO convertBeanToODDTO(List<LeaveRequestApproverDetailsBean> beans, Long id) {

        LeaveRequestApproverDetailsBean approverDetailsBean = beans.stream().filter(b -> b.getApproverDtlId().equals(id)).findFirst()
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        LeaveODRequestDTO leaveODRequestDTO = null;

        if (!ValidateUtils.isNullObject(approverDetailsBean.getEmpLeaveRqstId())) {

            leaveODRequestDTO = LeaveODRequestDTO.builder()
                    .requestId(approverDetailsBean.getEmpLeaveRqstId())
                    .empId(approverDetailsBean.getEmpId())
                    .employeeName(approverDetailsBean.getName())
                    .employeeCode(approverDetailsBean.getCode())
                    .applicationDateStr(DateHelper.formatDateObjectToUserViewDate(approverDetailsBean.getApplicationDate()))
                    .fromDateStr(DateHelper.formatDateObjectToUserViewDate(approverDetailsBean.getFromDate()))
                    .toDateStr(DateHelper.formatDateObjectToUserViewDate(approverDetailsBean.getToDate()))
                    .fromDateDuration(EnumConverter.toDisplayString(approverDetailsBean.getFromDateDuration(), DayType.class))
                    .toDateDuration(EnumConverter.toDisplayString(approverDetailsBean.getToDateDuration(), DayType.class))
                    .build();

            List<LeaveRequestApproverDetailsBean> filterBean = filterApproverList(beans);

            List<LeaveApproverDTO> approverList = filterBean.stream()
                    .map(bean -> LeaveApproverDTO.builder()
                            .id(bean.getApproverDtlId())
                            .sequence(bean.getSequence())
                            .approverId(bean.getApproverId())
                            .approverName(bean.getApproverName())
                            .approverCode(bean.getApproverCode())
                            .approverStatus(EnumConverter.toDisplayString(bean.getStatus(), ApplicationStatus.class))
                            .approverRemark(bean.getApproverRemark())
                            .actionDateTime(bean.getActionDateTime() == null ? null : DateHelper.formatDateTimeObjectToUserDateView(bean.getActionDateTime()))
                            .build())
                    .toList();

            leaveODRequestDTO.setApproverDTOList(approverList);
        }

        return LeaveApproverDTO.builder()
                .id(approverDetailsBean.getApproverDtlId())
                .sequence(approverDetailsBean.getSequence())
                .approverId(approverDetailsBean.getApproverId())
                .approverName(approverDetailsBean.getApproverName())
                .approverCode(approverDetailsBean.getApproverCode())
                .approverStatus(approverDetailsBean.getStatus())
                .approverRemark(approverDetailsBean.getApproverRemark())
                .actionDateTime(approverDetailsBean.getActionDateTime() == null ? null : DateHelper.formatDateTimeObjectToUserDateView(approverDetailsBean.getActionDateTime()))
                .leaveODRequestDTO(leaveODRequestDTO)
                .build();
    }


    private List<LeaveRequestApproverDetailsBean> filterApproverList(List<LeaveRequestApproverDetailsBean> beans) {

        List<LeaveRequestApproverDetailsBean> sorted = beans.stream().sorted(Comparator.comparing(LeaveRequestApproverDetailsBean::getSequence)).toList();
        List<LeaveRequestApproverDetailsBean> result = new ArrayList<>();
        for (LeaveRequestApproverDetailsBean b : sorted) {

            String status = b.getStatus();

            if ("APPROVED".equalsIgnoreCase(status)) {
                result.add(b);
                continue;
            }

            if ("REJECTED".equalsIgnoreCase(status)) {
                result.add(b);
                break;
            }

            if ("PENDING".equalsIgnoreCase(status)) {
                result.add(b);
                break;
            }

            if (status == null) {
                break;
            }
        }
        return result;
    }
}
