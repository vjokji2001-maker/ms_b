package com.smartcare.hrms.attendance.service.application.leave;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestApproverDetailsBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.LeaveRequstApproverConverter;
import com.smartcare.hrms.attendance.dao.application.leave.LeaveODApproverDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODApproverSearchDTO;
import com.smartcare.hrms.attendance.dto.application.OD.LeaveODRequestApproverDetailsDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveODRequestApproverDetailsRepository;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveODRequestRepository;
import com.smartcare.hrms.attendance.triggers.event.LeaveApprovedTrigger;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.repository.attendance.shift.EmployeeShiftRepository;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.ValidateUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class LeaveODRequestApproverDetailsService {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private LeaveODApproverDashboardDAO leaveODApproverDashboardDAO;

    @Autowired
    private LeaveODRequestRepository leaveODRequestRepository;

    @Autowired
    private LeaveODRequestApproverDetailsRepository leaveODRequestApproverDetailsRepository;

    @Autowired
    private LeaveRequstApproverConverter leaveRequstApproverConverter;

    private @Autowired EmployeeShiftRepository employeeShiftRepository;

    private @Autowired ApplicationEventPublisher eventPublisher;

    public PaginatedDataResponse<LeaveODApproverDashboardDTO> getPaginatedODRequestList(LeaveODApproverSearchDTO searchDTO) {

        if (searchDTO.getPaginationInfo() == null ||
                ValidateUtils.isZero(searchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        if (ValidateUtils.isStringEmpty(searchDTO.getApproverEmpId())) {
            throw new ValidationException(messageResource.getEnglishMessage("OD.approver.required"));
        }
        return leaveODApproverDashboardDAO.getData(searchDTO);
    }


    @Transactional
    public void approveSingleODRequestStatus(LeaveODRequestApproverDetailsDTO approverDetailsDTO, String clinicId){

        if (ValidateUtils.isLongZero(approverDetailsDTO.getId())){
            throw new ValidationException(messageResource.getEnglishMessage("OD.approver.required"));
        }

        LeaveODRequest leaveODRequest=leaveODRequestRepository.findById(UUID.fromString(approverDetailsDTO.getLeaveODRequestDTO().getRequestId()))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("OD.request.not.found")));

        boolean approverExists = leaveODRequest.getRequestDetails().stream()
                .anyMatch(approver -> approver.getId().equals(approverDetailsDTO.getId()));

        if (!approverExists){
            throw new ValidationException(messageResource.getEnglishMessage("OD.approver.not.found"));
        }

        AtomicBoolean statusChangedFlag = new AtomicBoolean(false);

        AtomicInteger finalStatusFlag = new AtomicInteger(0);

        leaveODRequest.getRequestDetails().forEach(approverObject ->{
            if (statusChangedFlag.get()){
                approverObject.setStatus(ApplicationStatus.PENDING);
                finalStatusFlag.set(1);
                statusChangedFlag.set(false);
            }

            if (String.valueOf(approverObject.getApproverId()).equals(approverDetailsDTO.getApproverId())) {
                ApplicationStatus applicationStatus = EnumConverter.toEnum(approverDetailsDTO.getOdStatus(), ApplicationStatus.class);
                approverObject.setStatus(applicationStatus);
                approverObject.setActionDateTime(LocalDateTime.now());
                approverObject.setRemark(approverDetailsDTO.getApproverRemark());

                if (ApplicationStatus.APPROVED.equals(applicationStatus)) {
                    statusChangedFlag.set(true);
                    finalStatusFlag.set(2);
                } else if (ApplicationStatus.REJECTED.equals(applicationStatus)) {
                    finalStatusFlag.set(3);
                }
            }
        });

        ApplicationStatus requestStatus = null;
        if (finalStatusFlag.get() == ApplicationStatus.IN_PROGRESS.ordinal()) {
            requestStatus = ApplicationStatus.IN_PROGRESS;
        } else if (finalStatusFlag.get() == ApplicationStatus.APPROVED.ordinal()) {
            requestStatus = ApplicationStatus.APPROVED;
        } else if (finalStatusFlag.get() == ApplicationStatus.REJECTED.ordinal()) {
            requestStatus = ApplicationStatus.REJECTED;
        }

        leaveODRequest.setStatus(requestStatus);
        leaveODRequest.setModifiedDateTime(LocalDateTime.now());
        leaveODRequest.setModifiedBy(clinicId);

        leaveODRequestRepository.save(leaveODRequest);

        if (ApplicationStatus.APPROVED.equals(requestStatus)) {
            LocalDate date = leaveODRequest.getFromDate();

            List<EmployeeShift> shift = employeeShiftRepository.findByEmployeeIdAndDate(UUID.fromString(leaveODRequest.getEmployeeId()), date);

            LeaveApprovedTrigger event = new LeaveApprovedTrigger(
                    this,
                    leaveODRequest.getEmployeeId(),
                    leaveODRequest.getFromDate(),
                    leaveODRequest.getToDate(),
                    leaveODRequest.getFromDateType(),
                    leaveODRequest.getToDateType(),
                    "OD",
                    shift.get(0).getId(),
                    leaveODRequest.getId()
            );

            eventPublisher.publishEvent(event);
        }

    }

    public LeaveApproverDTO getById(Long id){
        if (ValidateUtils.isLongZero(id)) {
            throw new ValidationException(messageResource.getEnglishMessage("OD.request.approver.not.found"));
        }
        List<LeaveRequestApproverDetailsBean> approverDetailBean=leaveODRequestApproverDetailsRepository.getByApproverDtlsId(id);
        if(CollectionUtils.isEmpty(approverDetailBean)){
            throw new ValidationException(messageResource.getEnglishMessage("OD.request.approver.not.found"));
        }
        return leaveRequstApproverConverter.convertBeanToDto(approverDetailBean,id);
    }



    public void approveBulkODRequestStatus(List<LeaveApproverDTO> approverDetailsDTOList, String clinicId){

        List<LeaveODRequest> requestTosSave = new ArrayList<>();

        for (LeaveApproverDTO leaveApproverDTO : approverDetailsDTOList) {

            if (ValidateUtils.isLongZero(leaveApproverDTO.getId())) {
                throw new ValidationException(messageResource.getEnglishMessage("OD.approver.required"));
            }

            LeaveODRequest leaveODRequest = leaveODRequestRepository.findById(UUID.fromString(leaveApproverDTO.getLeaveODRequestDTO().getRequestId()))
                            .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("OD.request.not.found")));

            boolean approverExist = leaveODRequest.getRequestDetails().stream()
                    .anyMatch(approver -> approver.getId().equals(leaveApproverDTO.getId()));

            if (!approverExist) {
                throw new ValidationException(messageResource.getEnglishMessage("OD.approver.not.found"));
            }

            AtomicBoolean statusChangedFlag = new AtomicBoolean(false);
            AtomicInteger finalStatusFlag = new AtomicInteger(0);

            leaveODRequest.getRequestDetails().forEach(approverObject -> {

                if (statusChangedFlag.get()) {
                    approverObject.setStatus(ApplicationStatus.PENDING);
                    finalStatusFlag.set(1);
                    statusChangedFlag.set(false);
                }

                if (String.valueOf(approverObject.getApproverId()).equals(leaveApproverDTO.getApproverId())) {

                    ApplicationStatus approverStatus = EnumConverter.toEnum(leaveApproverDTO.getApproverStatus(), ApplicationStatus.class);

                    approverObject.setStatus(approverStatus);
                    approverObject.setActionDateTime(LocalDateTime.now());
                    approverObject.setRemark(leaveApproverDTO.getApproverRemark());

                    if (ApplicationStatus.APPROVED.equals(approverStatus)) {
                        statusChangedFlag.set(true);
                        finalStatusFlag.set(2);
                    } else if (ApplicationStatus.REJECTED.equals(approverStatus)) {
                        finalStatusFlag.set(3);
                    }
                }
            });

            ApplicationStatus odStatus = null;

            if (finalStatusFlag.get() == ApplicationStatus.IN_PROGRESS.ordinal()) {
                odStatus = ApplicationStatus.IN_PROGRESS;
            }
            else if (finalStatusFlag.get() == ApplicationStatus.APPROVED.ordinal()) {
                odStatus = ApplicationStatus.APPROVED;
            }
            else if (finalStatusFlag.get() == ApplicationStatus.REJECTED.ordinal()) {
                odStatus = ApplicationStatus.REJECTED;
            }

            leaveODRequest.setStatus(odStatus);
            leaveODRequest.setModifiedBy(clinicId);
            leaveODRequest.setModifiedDateTime(LocalDateTime.now());

            requestTosSave.add(leaveODRequest);
            if (ApplicationStatus.APPROVED.equals(odStatus)) {


                LocalDate date = leaveODRequest.getFromDate();

                List<EmployeeShift> shift =
                        employeeShiftRepository.findByEmployeeIdAndDate(
                               UUID.fromString(leaveODRequest.getEmployeeId()), date);



                LeaveApprovedTrigger event = new LeaveApprovedTrigger(
                        this,
                        leaveODRequest.getEmployeeId(),
                        leaveODRequest.getFromDate(),
                        leaveODRequest.getToDate(),
                        leaveODRequest.getFromDateType(),
                        leaveODRequest.getToDateType(),
                        "OD",
                        shift.get(0).getId(),
                        leaveODRequest.getId()
                );

                eventPublisher.publishEvent(event);
            }
        }

        if (!requestTosSave.isEmpty()) {
            leaveODRequestRepository.saveAll(requestTosSave);
        }
    }



}
