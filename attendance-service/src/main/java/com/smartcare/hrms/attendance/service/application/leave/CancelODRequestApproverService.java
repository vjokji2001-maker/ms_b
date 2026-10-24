package com.smartcare.hrms.attendance.service.application.leave;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestApproverDetailsBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.CancelODRequestApproverConverter;
import com.smartcare.hrms.attendance.dao.application.leave.CancelODApproverDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.OD.CancelODApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.OD.CancelODApproverSearchDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveODRequestApproverDetailsRepository;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveODRequestRepository;
import com.smartcare.hrms.attendance.triggers.event.LeaveCancelTrigger;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.ValidateUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CancelODRequestApproverService {
    @Autowired
    private MessageResource messageResource;

    @Autowired
    private LeaveODRequestRepository leaveODRequestRepository;

    @Autowired
    private LeaveODRequestApproverDetailsRepository detailsRepository;

    @Autowired
    private CancelODApproverDashboardDAO approverDashboardDAO;

    @Autowired
    private CancelODRequestApproverConverter requestApproverConverter;

    private @Autowired ApplicationEventPublisher eventPublisher;

    public PaginatedDataResponse<CancelODApproverDashboardDTO> getODData(CancelODApproverSearchDTO approverSearchDTO) {

        if (ValidateUtils.isZero(approverSearchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        if (ValidateUtils.isStringEmpty(approverSearchDTO.getApproverEmpId())) {
            throw new ValidationException(messageResource.getEnglishMessage("cancel.OD.approver.required"));
        }
        return approverDashboardDAO.getData(approverSearchDTO);
    }


    @Transactional
    public void approveSingleCancelODRequestStatus(LeaveApproverDTO approverDTO, String clinicId) {

        if (ValidateUtils.isLongZero(approverDTO.getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("cancel.OD.approver.required"));
        }

        LeaveODRequest leaveODRequest = leaveODRequestRepository
                .findById(UUID.fromString(approverDTO.getLeaveODRequestDTO().getRequestId()))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("cancel.OD.request.not.found")));

        String odRequestId = leaveODRequest.getLeaveRequestId();

        boolean approverExist = leaveODRequest.getRequestDetails().stream()
                .anyMatch(approver -> approver.getId().equals(approverDTO.getId()));

        if (!approverExist) {
            throw new ValidationException(messageResource.getEnglishMessage("cancel.OD.approver.not.found"));
        }

        AtomicBoolean statusChangedFlag = new AtomicBoolean(false);
        AtomicInteger finalStatusFlag = new AtomicInteger(0);

        leaveODRequest.getRequestDetails().forEach(approverObject -> {

            if (statusChangedFlag.get()) {
                approverObject.setStatus(ApplicationStatus.PENDING);
                finalStatusFlag.set(ApplicationStatus.IN_PROGRESS.ordinal());
                statusChangedFlag.set(false);
            }

            if (String.valueOf(approverObject.getApproverId()).equals(approverDTO.getApproverId())) {

                ApplicationStatus approverStatus = EnumConverter.toEnum(approverDTO.getApproverStatus(), ApplicationStatus.class);

                approverObject.setStatus(approverStatus);
                approverObject.setActionDateTime(LocalDateTime.now());
                approverObject.setRemark(approverDTO.getApproverRemark());

                if (ApplicationStatus.APPROVED.equals(approverStatus)) {
                    statusChangedFlag.set(true);
                    finalStatusFlag.set(ApplicationStatus.APPROVED.ordinal());
                }

                if (ApplicationStatus.REJECTED.equals(approverStatus)) {
                    finalStatusFlag.set(ApplicationStatus.REJECTED.ordinal());
                }
            }
        });

        ApplicationStatus requestStatus = null;

        if (finalStatusFlag.get() == ApplicationStatus.IN_PROGRESS.ordinal()) {
            requestStatus = ApplicationStatus.IN_PROGRESS;
        }

        if (finalStatusFlag.get() == ApplicationStatus.APPROVED.ordinal()) {
            requestStatus = ApplicationStatus.APPROVED;
            int status= leaveODRequestRepository.updateODStatus(odRequestId);
        }

        if (finalStatusFlag.get() == ApplicationStatus.REJECTED.ordinal()) {
            requestStatus = ApplicationStatus.REJECTED;
        }

        leaveODRequest.setStatus(requestStatus);
        leaveODRequest.setModifiedDateTime(LocalDateTime.now());
        leaveODRequest.setModifiedBy(clinicId);

        leaveODRequestRepository.save(leaveODRequest);

        if(ApplicationStatus.APPROVED.equals(requestStatus)){

            LeaveCancelTrigger event= new LeaveCancelTrigger(
                    this,
                    leaveODRequest.getEmployeeId(),
                    leaveODRequest.getFromDate(),
                    leaveODRequest.getToDate()
            );
            eventPublisher.publishEvent(event);
        }
    }



    @Transactional
    public void approveBulkCancelODRequestStatus(List<LeaveApproverDTO> approverDTOList, String clinicId) {

        List<LeaveODRequest> requestToSave = new ArrayList<>();

        for (LeaveApproverDTO leaveApproverDTO : approverDTOList) {

            if (ValidateUtils.isLongZero(leaveApproverDTO.getId())) {
                throw new ValidationException(messageResource.getEnglishMessage("cancel.OD.approver.required"));
            }

            LeaveODRequest leaveODRequest = leaveODRequestRepository
                    .findById(UUID.fromString(leaveApproverDTO.getLeaveODRequestDTO().getRequestId()))
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("cancel.OD.request.not.found")));

            String odRequestId = leaveODRequest.getLeaveRequestId();

            boolean approverExist = leaveODRequest.getRequestDetails().stream()
                    .anyMatch(approver -> approver.getId().equals(leaveApproverDTO.getId()));

            if (!approverExist) {
                throw new ValidationException(messageResource.getEnglishMessage("cancel.OD.approver.not.found"));
            }

            AtomicBoolean statusChangedFlag = new AtomicBoolean();
            AtomicInteger finalStatusFlag = new AtomicInteger();

            leaveODRequest.getRequestDetails().forEach(approverObject -> {

                if (statusChangedFlag.get()) {
                    approverObject.setStatus(ApplicationStatus.PENDING);
                    statusChangedFlag.set(false);
                    finalStatusFlag.set(ApplicationStatus.IN_PROGRESS.ordinal());
                }

                if (String.valueOf(approverObject.getApproverId()).equals(leaveApproverDTO.getApproverId())) {

                    ApplicationStatus approverStatus =
                            EnumConverter.toEnum(leaveApproverDTO.getApproverStatus(), ApplicationStatus.class);

                    approverObject.setStatus(approverStatus);
                    approverObject.setActionDateTime(LocalDateTime.now());
                    approverObject.setRemark(leaveApproverDTO.getApproverRemark());

                    if (ApplicationStatus.APPROVED.equals(approverStatus)) {
                        statusChangedFlag.set(true);
                        finalStatusFlag.set(ApplicationStatus.APPROVED.ordinal());
                    } else if (ApplicationStatus.REJECTED.equals(approverStatus)) {
                        finalStatusFlag.set(ApplicationStatus.REJECTED.ordinal());
                    }
                }
            });

            ApplicationStatus requestStatus = null;

            if (finalStatusFlag.get() == ApplicationStatus.IN_PROGRESS.ordinal()) {
                requestStatus = ApplicationStatus.IN_PROGRESS;
            } else if (finalStatusFlag.get() == ApplicationStatus.APPROVED.ordinal()) {
                requestStatus = ApplicationStatus.APPROVED;
                leaveODRequestRepository.updateODStatus(odRequestId);
            } else if (finalStatusFlag.get() == ApplicationStatus.REJECTED.ordinal()) {
                requestStatus = ApplicationStatus.REJECTED;
            }

            leaveODRequest.setStatus(requestStatus);
            leaveODRequest.setModifiedBy(clinicId);
            leaveODRequest.setModifiedDateTime(LocalDateTime.now());

            requestToSave.add(leaveODRequest);

        if (ApplicationStatus.APPROVED.equals(requestStatus)) {
            LeaveCancelTrigger event = new LeaveCancelTrigger(
                    this,
                    leaveODRequest.getEmployeeId(),
                    leaveODRequest.getFromDate(),
                    leaveODRequest.getToDate()
            );
            eventPublisher.publishEvent(event);
        }
    }
        leaveODRequestRepository.saveAll(requestToSave);
    }



    public LeaveApproverDTO getODById(Long id) {

        if (ValidateUtils.isLongZero(id)) {
            throw new ValidationException(messageResource.getEnglishMessage(""));
        }
        List<LeaveRequestApproverDetailsBean> approverDetailBean = detailsRepository.getByApproverDtlsId(id);
        if (CollectionUtils.isEmpty(approverDetailBean)) {
            throw new ValidationException(messageResource.getEnglishMessage("OD.request.approver.not.found"));
        }
        return requestApproverConverter.convertBeanToODDTO(approverDetailBean, id);
    }


}
