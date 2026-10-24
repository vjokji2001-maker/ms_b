package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.common.bean.application.leave.LeaveODRequestBean;
import com.smartcare.hrms.attendance.dto.application.leave.CancelLeaveRequestDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequestDtls;
import com.smartcare.hrms.common.enumeration.ApplicationType;
import com.smartcare.hrms.common.bean.master.attendance.ApprovalGroupBean;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.EnumConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CancelODRequestConverter {

    public LeaveODRequest convertDtoToEntityForFreshEntity(List<ApprovalGroupBean> approvalGroupDtlsDTO, List<LeaveODRequestBean> leaveOdRequestDTO, CancelLeaveRequestDTO requestDTO) {
        LeaveODRequestBean leaveODRequestBean = leaveOdRequestDTO.get(0);
        List<LeaveODRequestDtls> approverList = new ArrayList<>();
        LeaveODRequest leaveODRequest = LeaveODRequest.builder()
                .applicationType(ApplicationType.OD_CANCEL)
                .employeeId(leaveODRequestBean.getRequestEmpId())
                .applicationDate(DateHelper.toLocalDate(leaveODRequestBean.getApplicationDate()))
                .fromDate(leaveODRequestBean.getFromDate())
                .fromDateType(leaveODRequestBean.getFromDateType())
                .toDate(leaveODRequestBean.getToDate())
                .toDateType(leaveODRequestBean.getToDateType())
                .status(ApplicationStatus.PENDING)
                .reason(requestDTO.getReason())
                .totalCount(leaveODRequestBean.getTotalCount())
                .leaveRequestId(requestDTO.getRequestId())
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

    public LeaveODRequestDTO convertBeanToDTO(List<LeaveODRequestBean> requestBean) {
        LeaveODRequestBean odRequestBean = requestBean.get(0);
        List<LeaveApproverDTO> approverList = new ArrayList<>();
        for (LeaveODRequestBean requestDetail : requestBean) {
            LeaveApproverDTO leaveApproverDTO = LeaveApproverDTO.builder()
                    .approverName(requestDetail.getApproverName())
                    .approverCode(requestDetail.getApproverCode())
                    .approverStatus(EnumConverter.toDisplayString(requestDetail.getApproverStatus(), ApplicationStatus.class))
                    .approverRemark(requestDetail.getRemark())
                    .actionDateTime(requestDetail.getActionDateTime() == null ? null : DateHelper.formatDateTimeObjectToUserDateView(requestDetail.getActionDateTime()))
                    .build();
            approverList.add(leaveApproverDTO);
        }
        return LeaveODRequestDTO.builder()
                .requestId(odRequestBean.getRequestId())
                .fromDateStr(DateHelper.formatDateObjectToUserViewDate(odRequestBean.getFromDate()))
                .toDateStr(DateHelper.formatDateObjectToUserViewDate(odRequestBean.getToDate()))
                .fromDateDuration(odRequestBean.getFromDateType().getValue())
                .toDateDuration(odRequestBean.getToDateType().getValue())
                .employeeId(odRequestBean.getRequestEmpId())
                .totalCount(odRequestBean.getTotalCount())
                .reason(odRequestBean.getReason())
                .status(odRequestBean.getApproverStatus())
                .approverDTOList(approverList)
                .build();
    }
}
