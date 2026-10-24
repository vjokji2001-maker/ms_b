package com.smartcare.hrms.attendance.service.application;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dao.application.ApproverDashboardDAO;
import com.smartcare.hrms.attendance.dao.application.CancelApproverDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.ApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.ApproverDashboardSearchDTO;
import com.smartcare.hrms.attendance.dto.application.CancelApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.CancelApproverSearchDTO;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApproverDashboardService {

    private @Autowired ApproverDashboardDAO approverDashboardDAO;
    private @Autowired CancelApproverDashboardDAO cancelApproverDashboardDAO;
    private @Autowired MessageResource messageResource;

    public PaginatedDataResponse<ApproverDashboardDTO> getApprovedData(ApproverDashboardSearchDTO dto){
        if (ValidateUtils.isZero(dto.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        if (ValidateUtils.isStringEmpty(dto.getApproverEmpId())){
            throw new ValidationException(messageResource.getEnglishMessage("leave.approver.required"));
        }
        return approverDashboardDAO.getData(dto);
    }

    public PaginatedDataResponse<CancelApproverDashboardDTO> getCancelApproverData(CancelApproverSearchDTO dto){
        if (ValidateUtils.isZero(dto.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        if (ValidateUtils.isStringEmpty(dto.getApproverEmpId())){
            throw new ValidationException(messageResource.getEnglishMessage("leave.approver.required"));
        }
        return cancelApproverDashboardDAO.getData(dto);
    }
}
