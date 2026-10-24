package com.smartcare.hrms.attendance.service.application.leave;

import com.smartcare.hrms.common.bean.application.leave.LeaveODRequestBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.LeaveODRequestConverter;
import com.smartcare.hrms.attendance.dao.application.leave.LeaveODRequestDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODRequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODRequestSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveODRequestRepository;
import com.smartcare.hrms.attendance.validator.LeaveODRequestValidator;
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
public class LeaveODRequestService {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private LeaveODRequestRepository leaveODRequestRepository;

    private @Autowired LeaveODRequestConverter leaveODRequestConverter;

    private @Autowired LeaveODRequestValidator leaveODRequestValidator;

    private @Autowired EmployeeConfigRepository employeeConfigRepository;

    private @Autowired LeaveODRequestDashboardDAO leaveODRequestDashboardDAO;

    @Transactional
    public void create(LeaveODRequestDTO requestDTO){
        leaveODRequestValidator.validateODRequest(requestDTO);

        List<ApprovalGroupBean> employeeApprovalGroupBean = employeeConfigRepository.getEmployeeApprovalGroup(requestDTO.getEmployeeId());
        if(CollectionUtils.isEmpty(employeeApprovalGroupBean)){
            throw new ValidationException(messageResource.getEnglishMessage("approval.group.not.assigned"));
        }

        LeaveODRequest leaveODRequest = leaveODRequestConverter.convertDtoToEntityForFreshEntry(employeeApprovalGroupBean, requestDTO);

        leaveODRequestRepository.save(leaveODRequest);
    }


    @Transactional
    public void delete(UUID id) {

        LeaveODRequest leaveODRequest = leaveODRequestRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("OD.request.not.found")));

        ApplicationStatus status = leaveODRequest.getStatus();

        if (status == ApplicationStatus.APPROVED) {
            throw new ValidationException(messageResource.getEnglishMessage("approved.OD.cannot.delete"));
        }

        if (status == ApplicationStatus.IN_PROGRESS) {
            throw new ValidationException(messageResource.getEnglishMessage("OD.cannot.delete.in.progress"));
        }

        leaveODRequestRepository.delete(leaveODRequest);
    }



    public LeaveODRequestDTO getById(String id) {

        if (ValidateUtils.isStringEmpty(id)) {
            throw new ValidationException(messageResource.getEnglishMessage("OD.request.not.found"));
        }

        List<LeaveODRequestBean> leaveODRequestBeans= leaveODRequestRepository.getODRequestById(id);

        if (CollectionUtils.isEmpty(leaveODRequestBeans)) {
            throw new ValidationException(messageResource.getEnglishMessage("OD.request.not.found"));
        }

        return leaveODRequestConverter.convertBeanToDto(leaveODRequestBeans);
    }


    public PaginatedDataResponse<LeaveODRequestDashboardDTO> getPaginatedODRequestList(LeaveODRequestSearchDTO searchDTO) {
        if (ValidateUtils.isZero(searchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        return leaveODRequestDashboardDAO.getData(searchDTO);
    }



}
