package com.smartcare.hrms.attendance.service.application.leave;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestApproverDetailsBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.LeaveRequstApproverConverter;
import com.smartcare.hrms.attendance.dao.application.leave.LeaveApproverDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveApproverDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveRequestApproverDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveRequestApproverSearchDTO;
import com.smartcare.hrms.common.entity.attendance.application.leave.LeaveODRequest;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveODRequestRepository;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveRequestDtlsRepository;
import com.smartcare.hrms.attendance.triggers.event.LeaveApprovedTrigger;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveName;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.repository.attendance.shift.EmployeeShiftRepository;
import com.smartcare.hrms.common.repository.master.attendance.leave.LeaveNameRepository;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.ValidateUtils;
import jakarta.transaction.Transactional;
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

@Service
public class LeaveRequestApproverService {
    private  @Autowired LeaveApproverDashboardDAO leaveApproverDashboardDAO;

    private @Autowired MessageResource messageResource;

    private @Autowired LeaveODRequestRepository leaveODRequestRepository;

    private @Autowired LeaveRequestDtlsRepository leaveRequestDtlsRepository;

    private @Autowired LeaveRequstApproverConverter leaveRequstApproverConverter;

    private @Autowired LeaveNameRepository leaveNameRepository;

    private @Autowired EmployeeShiftRepository employeeShiftRepository;

    private @Autowired ApplicationEventPublisher eventPublisher;

    public PaginatedDataResponse<LeaveRequestApproverDashboardDTO> getData(LeaveRequestApproverSearchDTO dto){
        if (ValidateUtils.isZero(dto.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        if (ValidateUtils.isStringEmpty(dto.getApproverEmpId())){
            throw new ValidationException(messageResource.getEnglishMessage("leave.approver.required"));
        }
        return leaveApproverDashboardDAO.getData(dto);
    }

    @Transactional
    public void approveSingleLeaveRequestStatus(LeaveApproverDTO approverDetailsDTO, String clinicId){

        if(ValidateUtils.isLongZero(approverDetailsDTO.getId())){
            throw new ValidationException(messageResource.getEnglishMessage("leave.approver.required"));
        }


        LeaveODRequest leaveODRequest= leaveODRequestRepository.findById(UUID.fromString(approverDetailsDTO.getLeaveODRequestDTO().getRequestId()))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("leave.request.not.found")));


        boolean approverExists= leaveODRequest.getRequestDetails().stream().
                anyMatch(approver -> approver.getId().equals(approverDetailsDTO.getId()));

        if(!approverExists){
            throw new ValidationException(messageResource.getEnglishMessage("leave.approver.not.found"));
        }

        AtomicBoolean statusChangedFlag= new AtomicBoolean(false);

        AtomicInteger finalStatusFlag= new AtomicInteger(0);

        leaveODRequest.getRequestDetails().forEach(approverObject->{
        if(statusChangedFlag.get()){
            approverObject.setStatus(ApplicationStatus.PENDING);
            finalStatusFlag.set(1);
            statusChangedFlag.set(false);
        }

        if(String.valueOf(approverObject.getApproverId()).equals(approverDetailsDTO.getApproverId())){
            ApplicationStatus approverStatus= EnumConverter.toEnum(approverDetailsDTO.getApproverStatus(), ApplicationStatus.class);
            approverObject.setStatus(approverStatus);
            approverObject.setActionDateTime(LocalDateTime.now());
            approverObject.setRemark(approverDetailsDTO.getApproverRemark());
            if(ApplicationStatus.APPROVED.equals(approverStatus)){
                statusChangedFlag.set(true);
                finalStatusFlag.set(2);

            } else if (ApplicationStatus.REJECTED.equals(approverStatus)) {
                finalStatusFlag.set(3);
            }
        }
        });

        ApplicationStatus requestStatus= null;
        if(finalStatusFlag.get()== ApplicationStatus.IN_PROGRESS.ordinal()){
            requestStatus=ApplicationStatus.IN_PROGRESS;
        }
        if(finalStatusFlag.get()== ApplicationStatus.APPROVED.ordinal()){
            requestStatus=ApplicationStatus.APPROVED;


        }
        if(finalStatusFlag.get()==ApplicationStatus.REJECTED.ordinal()){
            requestStatus=ApplicationStatus.REJECTED;
        }

        leaveODRequest.setStatus(requestStatus);

        leaveODRequest.setModifiedDateTime(LocalDateTime.now());
        leaveODRequest.setModifiedBy(clinicId);
        leaveODRequestRepository.save(leaveODRequest);

        if (ApplicationStatus.APPROVED.equals(requestStatus)) {
            LeaveName leaveName = leaveNameRepository.findById(leaveODRequest.getLeaveNameId())
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("leave.name.not.found")));

            LocalDate date =leaveODRequest.getFromDate();

            List<EmployeeShift> shift = employeeShiftRepository.findByEmployeeIdAndDate(UUID.fromString(leaveODRequest.getEmployeeId()), date);

            LeaveApprovedTrigger event = new LeaveApprovedTrigger(
                    this,
                    leaveODRequest.getEmployeeId(),
                    leaveODRequest.getFromDate(),
                    leaveODRequest.getToDate(),
                    leaveODRequest.getFromDateType(),
                    leaveODRequest.getToDateType(),
                    leaveName.getCode(),
                    shift.get(0).getId(),
                    leaveODRequest.getId()
            );

            eventPublisher.publishEvent(event);


        }
    }

    public LeaveApproverDTO getById(Long id){

        if (ValidateUtils.isLongZero(id)) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.request.approver.not.found"));
        }

        List<LeaveRequestApproverDetailsBean> approverDetailBean=leaveRequestDtlsRepository.getByApproverDtlsId(id);
        if(CollectionUtils.isEmpty(approverDetailBean)){
            throw new ValidationException(messageResource.getEnglishMessage("leave.request.approver.not.found"));
        }
        return leaveRequstApproverConverter.convertBeanToDto(approverDetailBean,id);

    }

    public void approveBulkLeaveRequestStatus(List<LeaveApproverDTO> approverDetailsDTOList, String clinicId){

        List<LeaveODRequest> requestToSave= new ArrayList<>();

        for(LeaveApproverDTO leaveApproverDTO:approverDetailsDTOList ) {
            if (ValidateUtils.isLongZero(leaveApproverDTO.getId())) {
                throw new ValidationException(messageResource.getEnglishMessage("leave.approver.required"));
            }

            LeaveODRequest leaveODRequest = leaveODRequestRepository.findById(UUID.fromString(leaveApproverDTO.getLeaveODRequestDTO().getRequestId()))
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("leave.request.not.found")));

            boolean approverExist = leaveODRequest.getRequestDetails().stream().
                    anyMatch(approver -> approver.getId().equals(leaveApproverDTO.getId()));

            if (!approverExist) {
                throw new ValidationException(messageResource.getEnglishMessage("leave.approver.not.found"));
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

                    } else if (ApplicationStatus.REJECTED.equals(approverStatus))
                        finalStatusFlag.set(3);
                }
            });

            ApplicationStatus leaveStatus = null;

            if (finalStatusFlag.get() == ApplicationStatus.IN_PROGRESS.ordinal()) {
                leaveStatus = ApplicationStatus.IN_PROGRESS;
            } else if (finalStatusFlag.get() == ApplicationStatus.APPROVED.ordinal()) {
                leaveStatus = ApplicationStatus.APPROVED;
            } else if (finalStatusFlag.get() == ApplicationStatus.REJECTED.ordinal()) {
                leaveStatus = ApplicationStatus.REJECTED;
            }
            leaveODRequest.setStatus(leaveStatus);
            leaveODRequest.setModifiedBy(clinicId);
            leaveODRequest.setModifiedDateTime(LocalDateTime.now());

            requestToSave.add(leaveODRequest);

            if (ApplicationStatus.APPROVED.equals(leaveStatus)) {

                LeaveName leaveName = leaveNameRepository
                        .findById(leaveODRequest.getLeaveNameId())
                        .orElseThrow(() -> new ValidationException("Leave name not found"));

                LocalDate date =leaveODRequest.getFromDate();

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
                        leaveName.getCode(),
                        shift.get(0).getId(),
                        leaveODRequest.getId()
                );

                eventPublisher.publishEvent(event);
            }
        }
            if(!requestToSave.isEmpty()){
                leaveODRequestRepository.saveAll(requestToSave);


        }


    }

    }

