package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.common.bean.application.leave.LeaveODRequestBean;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequestDtls;
import com.smartcare.hrms.common.enumeration.ApplicationType;
import com.smartcare.hrms.common.bean.master.attendance.ApprovalGroupBean;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.EnumConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LeaveODRequestConverter {

    public LeaveODRequest convertDtoToEntityForFreshEntry(List<ApprovalGroupBean> approvalGroupDtlsDTO, LeaveODRequestDTO requestDTO) {
        List<LeaveODRequestDtls> approverList = new ArrayList<>();
        LeaveODRequest leaveODRequest = LeaveODRequest.builder()
                .applicationType(ApplicationType.OD_RQST)
                .employeeId(requestDTO.getEmployeeId())
                .applicationDate(requestDTO.getApplicationDate())
                .fromDate(requestDTO.getFromDate())
                .toDate(requestDTO.getToDate())
                .fromDateType(EnumConverter.toEnum(requestDTO.getFromDateDuration(), DayType.class))
                .toDateType(EnumConverter.toEnum(requestDTO.getToDateDuration(), DayType.class))
                .reason(requestDTO.getReason())
                .status(ApplicationStatus.PENDING)
                .totalCount(requestDTO.getTotalCount())
                .build();
        for (ApprovalGroupBean bean : approvalGroupDtlsDTO) {
            LeaveODRequestDtls leaveODRequestDtls = LeaveODRequestDtls.builder()
                    .leaveODRequest(leaveODRequest)
                    .status(bean.getApprovalSequence() == 1 ? ApplicationStatus.PENDING : null)
                    .sequence(bean.getApprovalSequence())
                    .approverId(bean.getApproverEmpId())
                    .build();
            approverList.add(leaveODRequestDtls);
        }
        leaveODRequest.setRequestDetails(approverList);
        return leaveODRequest;
    }


    public LeaveODRequestDTO convertBeanToDto(List<LeaveODRequestBean> requestBeans) {
        LeaveODRequestBean odRequestBean = requestBeans.get(0);
        List<LeaveApproverDTO> approverList = new ArrayList<>();
        for (LeaveODRequestBean requestDetail : requestBeans) {
            LeaveApproverDTO approverDTO = LeaveApproverDTO.builder()
                    .approverName(requestDetail.getApproverName())
                    .approverCode(requestDetail.getApproverCode())
                    .approverStatus(EnumConverter.toDisplayString(requestDetail.getApproverStatus(), ApplicationStatus.class))
                    .approverRemark(requestDetail.getRemark())
                    .actionDateTime(requestDetail.getActionDateTime() == null ? null : DateHelper.formatDateTimeObjectToUserDateView(requestDetail.getActionDateTime()))
                    .build();
            approverList.add(approverDTO);
        }
        return LeaveODRequestDTO.builder()
                .requestId(odRequestBean.getRequestId())
                .applicationDate(odRequestBean.getApplicationDate())
                .fromDateStr(DateHelper.formatDateObjectToUserDateView(odRequestBean.getFromDate()))
                .toDateStr(DateHelper.formatDateObjectToUserDateView(odRequestBean.getToDate()))
                .fromDateDuration(odRequestBean.getFromDateType().getValue())
                .toDateDuration(odRequestBean.getToDateType().getValue())
                .employeeId(odRequestBean.getRequestEmpId())
                .totalCount(odRequestBean.getTotalCount())
                .reason(odRequestBean.getReason())
                .status(EnumConverter.toDisplayString(odRequestBean.getStatus(), ApplicationStatus.class))
                .approverDTOList(approverList)
                .build();
    }

}