package com.smartcare.hrms.attendance.service.application.leave;

import com.smartcare.hrms.common.bean.application.leave.LeaveODRequestBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.CancelODRequestConverter;
import com.smartcare.hrms.attendance.dao.application.leave.CancelODRequestDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.OD.CancelODRequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelLeaveRequestDTO;
import com.smartcare.hrms.attendance.dto.application.leave.CancelRequestSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveODRequestRepository;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveRequestRepository;
import com.smartcare.hrms.attendance.validator.CancelODRequestValidator;
import com.smartcare.hrms.common.bean.master.attendance.ApprovalGroupBean;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.repository.employee.EmployeeConfigRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Service
public class CancelODRequestService {
    private @Autowired MessageResource messageResource;

    private @Autowired LeaveODRequestRepository leaveODRequestRepository;

    private @Autowired LeaveRequestRepository leaveRequestRepository;

    private @Autowired EmployeeConfigRepository employeeConfigRepository;

    private @Autowired CancelODRequestValidator cancelODRequestValidator;

    private @Autowired CancelODRequestConverter cancelODRequestConverter;

    private @Autowired CancelODRequestDashboardDAO cancelODRequestDashboardDao;

    public PaginatedDataResponse<CancelODRequestDashboardDTO> getDate(CancelRequestSearchDTO searchDTO){
        if (ValidateUtils.isZero(searchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        return cancelODRequestDashboardDao.getData(searchDTO);
    }

    @Transactional
    public void create(CancelLeaveRequestDTO requestDTO){

        cancelODRequestValidator.validateCancelODRequest(requestDTO);
        List<ApprovalGroupBean> employeeApprovalGroupBean = employeeConfigRepository.getEmployeeApprovalGroup(requestDTO.getEmployeeId());
        if(CollectionUtils.isEmpty(employeeApprovalGroupBean)){
            throw new ValidationException(messageResource.getEnglishMessage("approval.group.not.assigned"));
        }

        List<LeaveODRequestBean>  empApprovedOD = leaveODRequestRepository.getODRequestById(requestDTO.getRequestId());


        LeaveODRequest leaveODRequest = cancelODRequestConverter.convertDtoToEntityForFreshEntity(employeeApprovalGroupBean, empApprovedOD, requestDTO );

        leaveODRequestRepository.save(leaveODRequest);

    }

    public LeaveODRequestDTO getById(String id) {

        if (ValidateUtils.isStringEmpty(id)) {
            throw new ValidationException(messageResource.getEnglishMessage("OD.request.not.found"));
        }

        List<LeaveODRequestBean> odRequestBeans = leaveODRequestRepository.getCancelODRequestById(id);

        if (CollectionUtils.isEmpty(odRequestBeans)) {
            throw new ValidationException(messageResource.getEnglishMessage("OD.request.not.found"));
        }
        return cancelODRequestConverter.convertBeanToDTO(odRequestBeans);
    }

    public void delete(UUID id){

        LeaveODRequest leaveODRequest = leaveODRequestRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.cancel.OD.record.not.found")));

        ApplicationStatus status = leaveODRequest.getStatus();
        if (status == ApplicationStatus.APPROVED) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.OD.cannot.delete.in.approved.phase"));
        }
        if (status == ApplicationStatus.IN_PROGRESS){
            throw new ValidationException(messageResource.getEnglishMessage("employee.OD.cannot.delete.in.progress.phase"));
        }

        leaveODRequestRepository.delete(leaveODRequest);

    }


}
