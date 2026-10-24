package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestApproverDetailsBean;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.enumeration.ApplicationType;
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
public class LeaveRequstApproverConverter {

    public LeaveApproverDTO convertBeanToDto(List<LeaveRequestApproverDetailsBean> beans, Long id){
        LeaveRequestApproverDetailsBean approverDetailsBean= beans.stream().filter(b -> b.getApproverDtlId().equals(id))
                .findFirst().orElseThrow(null);



        LeaveODRequestDTO leaveODRequestDTO=null;
        if(!ValidateUtils.isNullObject(approverDetailsBean.getEmpLeaveRqstId())){
            leaveODRequestDTO = LeaveODRequestDTO.builder()
                    .requestId(approverDetailsBean.getEmpLeaveRqstId())
                    .empId(approverDetailsBean.getEmpId())
                    .employeeName(approverDetailsBean.getName())
                    .employeeCode(approverDetailsBean.getCode())
                    .applicationType(EnumConverter.toDisplayString(approverDetailsBean.getApplicationType(), ApplicationType.class))
                    .applicationDateStr(DateHelper.formatDateObjectToUserViewDate(approverDetailsBean.getApplicationDate()))
                    .fromDateStr(DateHelper.formatDateObjectToUserViewDate(approverDetailsBean.getFromDate()))
                    .toDateStr(DateHelper.formatDateObjectToUserViewDate(approverDetailsBean.getToDate()))
                    .fromDateDuration(EnumConverter.toDisplayString(approverDetailsBean.getFromDateDuration(), DayType.class))
                    .toDateDuration(EnumConverter.toDisplayString(approverDetailsBean.getToDateDuration(), DayType.class))
                    .leaveName(approverDetailsBean.getLeaveName())
                    .reason(approverDetailsBean.getReason())
                    .build();

            List<LeaveRequestApproverDetailsBean> filterBean= filterApproverList(beans);

            List<LeaveApproverDTO> approverList=filterBean.stream()
                    .map(bean -> LeaveApproverDTO.builder()
                            .id(bean.getApproverDtlId())
                            .sequence(bean.getSequence())
                            .approverId(bean.getApproverId())
                            .approverName(bean.getApproverName())
                            .approverCode(bean.getApproverCode())
                            .approverStatus( EnumConverter.toDisplayString(bean.getStatus(), ApplicationStatus.class))
                            .approverRemark(bean.getApproverRemark())
                            .actionDateTime((bean.getActionDateTime()  == null ? null : DateHelper.formatDateTimeObjectToUserDateView(bean.getActionDateTime())))
                            .build()).toList();
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
                .actionDateTime(approverDetailsBean.getActionDateTime()  == null ? null : DateHelper.formatDateTimeObjectToUserDateView(approverDetailsBean.getActionDateTime()))
                .leaveODRequestDTO(leaveODRequestDTO)
                .build();
    }

    private List<LeaveRequestApproverDetailsBean> filterApproverList(List<LeaveRequestApproverDetailsBean> beans) {

        List<LeaveRequestApproverDetailsBean> sorted = beans.stream()
                .sorted(Comparator.comparing(LeaveRequestApproverDetailsBean::getSequence))
                .toList();

        List<LeaveRequestApproverDetailsBean> result = new ArrayList<>();

        for (LeaveRequestApproverDetailsBean b : sorted) {
            String status = b.getStatus();

            // Add APPROVED and continue
            if ("APPROVED".equalsIgnoreCase(status)) {
                result.add(b);
                continue;
            }

            // REJECTED add and stop
            if ("REJECTED".equalsIgnoreCase(status)) {
                result.add(b);
                break;
            }

            // First pending approver add & stop
            if ("PENDING".equalsIgnoreCase(status)) {
                result.add(b);
                break;
            }

            // Null stop without adding
            if (status == null) {
                break;
            }
        }
        return result;
    }
}
