package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestBean;
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
public class LeaveRequestConverter {

    public LeaveODRequest convertDtoToEntityForFreshEntry(List<ApprovalGroupBean> approvalGroupDtlsDTO, LeaveODRequestDTO requestDTO){
        List<LeaveODRequestDtls> approverList = new ArrayList<>();
        LeaveODRequest leaveODRequest = LeaveODRequest.builder()
                .applicationType(ApplicationType.LEAVE_RQST)
                .employeeId(requestDTO.getEmployeeId())
                .leaveNameId(requestDTO.getLeaveNameId())
                .applicationDate(requestDTO.getApplicationDate())
                .yearId(requestDTO.getYearId())
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


    public LeaveODRequestDTO convertBeanToDTO(List<LeaveRequestBean> requestBean){
        LeaveRequestBean leaveRequestBean = requestBean.get(0);
        List<LeaveApproverDTO> approverList = new ArrayList<>();
        for (LeaveRequestBean requestDetail : requestBean) {
            LeaveApproverDTO leaveApproverDTO = LeaveApproverDTO.builder()
                    .approverName(requestDetail.getApproverName())
                    .approverCode(requestDetail.getApproverCode())
                    .approverStatus(EnumConverter.toDisplayString(requestDetail.getApproverStatus(), ApplicationStatus.class))
                    .approverRemark(requestDetail.getRemark())
                    .actionDateTime(requestDetail.getActionDateTime()  == null ? null : DateHelper.formatDateTimeObjectToUserDateView(requestDetail.getActionDateTime()))
                    .build();
            approverList.add(leaveApproverDTO);
        }
        return LeaveODRequestDTO.builder()
                .requestId(leaveRequestBean.getLeaveRequestId())
                .leaveNameId(leaveRequestBean.getLeaveNameId())
                .leaveName(leaveRequestBean.getLeaveName())
                .applicationType(EnumConverter.toDisplayString(leaveRequestBean.getApplicationType(), ApplicationType.class))
                .applicationDateStr(DateHelper.formatDateObjectToUserViewDate(leaveRequestBean.getApplicationDate()))
                .fromDateStr(DateHelper.formatDateObjectToUserViewDate(leaveRequestBean.getFromDate()))
                .toDateStr(DateHelper.formatDateObjectToUserViewDate(leaveRequestBean.getToDate()))
                .fromDateDuration(leaveRequestBean.getFromDateType().getValue())
                .toDateDuration(leaveRequestBean.getToDateType().getValue())
                .employeeId(leaveRequestBean.getEmpId())
                .totalCount(leaveRequestBean.getTotalLeaveCount())
                .yearId(leaveRequestBean.getYearId())
                .reason(leaveRequestBean.getReason())
                .status(EnumConverter.toDisplayString(leaveRequestBean.getStatus(), ApplicationStatus.class))
                .approverDTOList(approverList)
                .build();
    }


}
