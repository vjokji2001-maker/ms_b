package com.smartcare.hrms.attendance.service.application.leave;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.LeaveRequestConverter;
import com.smartcare.hrms.attendance.dao.application.leave.LeaveDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveRequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveRequestSearchDTO;
import com.smartcare.hrms.attendance.validator.LeaveRequestValidator;
import com.smartcare.hrms.common.bean.master.attendance.ApprovalGroupBean;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveRequestRepository;
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
public class LeaveRequestService {


    @Autowired
    private MessageResource messageResource;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    private @Autowired LeaveRequestConverter leaveRequestConverter;

    private @Autowired LeaveRequestValidator leaveRequestValidator;

    private @Autowired EmployeeConfigRepository employeeConfigRepository;

    private  @Autowired LeaveDashboardDAO leaveDashboardDAO;

    @Transactional
    public void create(LeaveODRequestDTO requestDTO){
        //Validating all request with multiple valid conditions
        leaveRequestValidator.validateLeaveRequest(requestDTO);

        // fetch approval details
        List<ApprovalGroupBean> employeeApprovalGroupBean = employeeConfigRepository.getEmployeeApprovalGroup(requestDTO.getEmployeeId());
        if(CollectionUtils.isEmpty(employeeApprovalGroupBean)){
            throw new ValidationException(messageResource.getEnglishMessage("approval.group.not.assigned"));
        }

        //Convert to entity
        LeaveODRequest leaveODRequest = leaveRequestConverter.convertDtoToEntityForFreshEntry(employeeApprovalGroupBean, requestDTO);

        // save leave request
        leaveRequestRepository.save(leaveODRequest);
    }



    public void delete(UUID id){

    LeaveODRequest leaveODRequest= leaveRequestRepository.findById(id)
            .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.leave.record.not.found")));

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



    public LeaveODRequestDTO getById(String id) {

        if (ValidateUtils.isStringEmpty(id)) {
            throw new ValidationException(messageResource.getEnglishMessage("leave.request.not.found"));
        }

        List<LeaveRequestBean> leaveRequestBeans = leaveRequestRepository.getLeaveRequestById(id);

        if (CollectionUtils.isEmpty(leaveRequestBeans)) {
            throw new ValidationException(messageResource.getEnglishMessage("leave.request.not.found"));
        }

        return leaveRequestConverter.convertBeanToDTO(leaveRequestBeans);
    }


    public PaginatedDataResponse<LeaveRequestDashboardDTO> dashboard(LeaveRequestSearchDTO searchDTO){
        if (ValidateUtils.isZero(searchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        return  leaveDashboardDAO.getData(searchDTO);
    }

}
