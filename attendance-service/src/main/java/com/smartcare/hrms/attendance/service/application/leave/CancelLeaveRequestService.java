package com.smartcare.hrms.attendance.service.application.leave;


import com.smartcare.hrms.common.bean.application.leave.LeaveRequestBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.CancelLeaveRequestConverter;
import com.smartcare.hrms.attendance.dao.application.leave.CancelRequestDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelLeaveRequestDTO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelRequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelRequestSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;

import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveODRequestRepository;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveRequestRepository;
import com.smartcare.hrms.attendance.validator.CancelLeaveRequestValidator;
import com.smartcare.hrms.common.bean.master.attendance.ApprovalGroupBean;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.globalexception.ValidationException;

import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.repository.employee.EmployeeConfigRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import java.util.List;
import java.util.UUID;

@Service
public class CancelLeaveRequestService {

    private @Autowired MessageResource messageResource;

    private @Autowired LeaveODRequestRepository leaveODRequestRepository;

    private @Autowired LeaveRequestRepository leaveRequestRepository;

    private @Autowired EmployeeConfigRepository employeeConfigRepository;

    private @Autowired CancelLeaveRequestValidator cancelLeaveRequestValidator;

    private @Autowired CancelLeaveRequestConverter cancelLeaveRequestConverter;

    private @Autowired CancelRequestDashboardDAO cancelRequestDashboardDao;


    public PaginatedDataResponse<CancelRequestDashboardDTO> getDate(CancelRequestSearchDTO searchDTO){
        if (ValidateUtils.isZero(searchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        return cancelRequestDashboardDao.getData(searchDTO);
    }

    @Transactional
    public void create(CancelLeaveRequestDTO requestDTO){

        cancelLeaveRequestValidator.validateCancelLeaveRequest(requestDTO);
        List<ApprovalGroupBean> employeeApprovalGroupBean = employeeConfigRepository.getEmployeeApprovalGroup(requestDTO.getEmployeeId());
        if(CollectionUtils.isEmpty(employeeApprovalGroupBean)){
            throw new ValidationException(messageResource.getEnglishMessage("approval.group.not.assigned"));
        }

        List<LeaveRequestBean>  empApprovedLeave= leaveRequestRepository.getLeaveRequestById(requestDTO.getRequestId());


    LeaveODRequest leaveODRequest= cancelLeaveRequestConverter.convertDtoToEntityForFreshEntity(employeeApprovalGroupBean,empApprovedLeave, requestDTO );

    leaveODRequestRepository.save(leaveODRequest);

    }

    public LeaveODRequestDTO getById(String id) {

        if (ValidateUtils.isStringEmpty(id)) {
            throw new ValidationException(messageResource.getEnglishMessage("leave.request.not.found"));
        }

        List<LeaveRequestBean> leaveRequestBeans = leaveRequestRepository.getCancelLeaveRequestById(id);

        if (CollectionUtils.isEmpty(leaveRequestBeans)) {
            throw new ValidationException(messageResource.getEnglishMessage("leave.request.not.found"));
        }
        return cancelLeaveRequestConverter.convertBeanToDTO(leaveRequestBeans);
    }

    public void delete(UUID id){

        LeaveODRequest leaveODRequest= leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.cancel.leave.record.not.found")));

        ApplicationStatus status= leaveODRequest.getStatus();
        if (status == ApplicationStatus.APPROVED) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.leave.cannot.delete.in.approved.phase"));
        }
        // If In-Progress, do not allow deletion
        if (status == ApplicationStatus.IN_PROGRESS){
            throw new ValidationException(messageResource.getEnglishMessage("employee.leave.cannot.delete.in.progress.phase"));
        }

        leaveRequestRepository.delete(leaveODRequest);

    }
}
