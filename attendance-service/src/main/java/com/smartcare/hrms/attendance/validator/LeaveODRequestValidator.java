package com.smartcare.hrms.attendance.validator;

import com.smartcare.hrms.common.bean.application.leave.LeaveODRequestBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveODRequestRepository;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeaveODRequestValidator {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private LeaveODRequestRepository leaveODRequestRepository;

    public void validateODRequest(LeaveODRequestDTO requestDTO) {

        validateRequestDataDTO(requestDTO);
        validateInvalidDayTypeRequest(requestDTO);
        List<LeaveODRequestBean> empExistingApprovedPendingLeavs = leaveODRequestRepository.getEmployeeLeaveRequestDataByEmpId(requestDTO.getEmployeeId());

        if(!CollectionUtils.isEmpty(empExistingApprovedPendingLeavs)){
            validateRequestedDatesWithExistingDate(empExistingApprovedPendingLeavs, requestDTO);

            validateDatesIfExistInBetween(empExistingApprovedPendingLeavs,
                    requestDTO.getFromDate(),
                    requestDTO.getToDate());
        }
    }

    private void validateRequestDataDTO(LeaveODRequestDTO requestDTO){
        if(ValidateUtils.isStringEmpty(requestDTO.getEmployeeId()) ||
                ValidateUtils.isNullObject(employeeRepository.getEmpId(requestDTO.getEmployeeId()))){
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }
        if(requestDTO.getFromDate() == null || requestDTO.getToDate() == null){
            throw new ValidationException(messageResource.getEnglishMessage("invalid.date.range"));
        }
        if(requestDTO.getFromDateDuration() == null || requestDTO.getToDateDuration() == null){
            throw new ValidationException(messageResource.getEnglishMessage("day.type.required"));
        }
        if(requestDTO.getReason() == null || requestDTO.getReason().trim().isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("reason.required"));
        }
    }

    private void validateInvalidDayTypeRequest(LeaveODRequestDTO requestDTO){
        if(requestDTO.getFromDate().isAfter(requestDTO.getToDate())){
            throw new ValidationException(messageResource.getEnglishMessage("invalid.date.range"));
        }else if(requestDTO.getToDate().isAfter(requestDTO.getFromDate())){
            if(DayType.FIRST_HALF.toString().equals(requestDTO.getFromDateDuration())){
                throw new ValidationException(messageResource.getEnglishMessage("invalid.day.type"));
            } else if (DayType.SECOND_HALF.toString().equals(requestDTO.getToDateDuration())) {
                throw new ValidationException(messageResource.getEnglishMessage("invalid.day.type"));
            }
        }
    }

    private void validateDatesIfExistInBetween(List<LeaveODRequestBean> empExistingApprovedPendingLeavs, LocalDate leaveFromDate, LocalDate leaveToDate){

        boolean fromDateAlreadyExstFlag = empExistingApprovedPendingLeavs.stream()
                .anyMatch(p -> leaveFromDate.isAfter(p.getFromDate())
                        && leaveFromDate.isBefore(p.getToDate()));
        if(fromDateAlreadyExstFlag){
            throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
        }

        boolean toDateAlreadyExstFlag = empExistingApprovedPendingLeavs.stream()
                .anyMatch(p -> leaveToDate.isAfter(p.getFromDate())
                        && leaveToDate.isBefore(p.getToDate()));

        if(toDateAlreadyExstFlag){
            throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
        }
    }

    private void validateRequestedDatesWithExistingDate(List<LeaveODRequestBean> empExistingApprovedPendingLeavs,
                                                        LeaveODRequestDTO requestDTO) {

        LocalDate leaveFromDate = requestDTO.getFromDate();
        LocalDate leaveToDate   = requestDTO.getToDate();

        if (leaveFromDate.isBefore(leaveToDate)) {
            {
                List<LeaveODRequestBean> leaveODRequestBeans = empExistingApprovedPendingLeavs.stream()
                        .filter(p -> p.getFromDate().equals(leaveFromDate)).collect(Collectors.toList());
                if (leaveODRequestBeans.size() == 2) {
                    throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
                } else if (!leaveODRequestBeans.isEmpty()) {
                    LeaveODRequestBean leaveRequestBean = leaveODRequestBeans.get(0);
                    if (leaveRequestBean.getFromDateType().equals(DayType.FULL_DAY) || leaveRequestBean.getFromDateType().equals(DayType.fromValue(requestDTO.getFromDateDuration()))) {
                        throw new ValidationException(messageResource.getEnglishMessage(
                                "fromdate.already.exists.for.requested.period"));
                    }
                }
            }
            {
                List<LeaveODRequestBean> leaveODRequestBeans = empExistingApprovedPendingLeavs.stream().filter(p -> p.getFromDate().equals(leaveToDate)).collect(Collectors.toList());
                if (leaveODRequestBeans.size() == 2) {
                    throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
                } else if (!leaveODRequestBeans.isEmpty()) {
                    LeaveODRequestBean leaveRequestBean = leaveODRequestBeans.get(0);
                    if (leaveRequestBean.getToDateType().equals(DayType.FULL_DAY) || leaveRequestBean.getToDateType().equals(DayType.fromValue(requestDTO.getFromDateDuration()))) {
                        throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
                    }
                }
            }
            {
                List<LeaveODRequestBean> leaveODRequestBeans = empExistingApprovedPendingLeavs.stream().filter(p -> p.getToDate().equals(leaveFromDate))
                        .collect(Collectors.toList());
                if (leaveODRequestBeans.size() == 2) {
                    throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
                } else if (!leaveODRequestBeans.isEmpty()) {
                    LeaveODRequestBean leaveRequestBean = leaveODRequestBeans.get(0);
                    if (leaveRequestBean.getFromDateType().equals(DayType.FULL_DAY) || leaveRequestBean.getFromDateType().equals(DayType.fromValue(requestDTO.getToDateDuration()))) {
                        throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
                    }
                }
            }
            {
                List<LeaveODRequestBean> leaveODRequestBeans =
                        empExistingApprovedPendingLeavs.stream().filter(p -> p.getToDate().equals(leaveToDate))
                                .collect(Collectors.toList());
                if (leaveODRequestBeans.size() == 2) {
                    throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
                } else if (!leaveODRequestBeans.isEmpty()) {
                    LeaveODRequestBean leaveRequestBean = leaveODRequestBeans.get(0);
                    if (leaveRequestBean.getToDateType().equals(DayType.FULL_DAY) || leaveRequestBean.getToDateType().equals(DayType.fromValue(requestDTO.getToDateDuration()))) {
                        throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
                    }
                }
            }
        } else {
            boolean isLeaveAlreadyExist = empExistingApprovedPendingLeavs.stream()
                    .anyMatch(p -> p.getFromDate().equals(leaveFromDate) && (p.getFromDateType().equals(DayType.FULL_DAY) || p.getFromDateType()
                                    .equals(DayType.fromValue(requestDTO.getFromDateDuration()))));
            if (isLeaveAlreadyExist) {
                throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
            }
        }
    }

    private void subValidatorForExistingLeave(List<LeaveODRequestBean> existingLeaveODRequestBeans, DayType durationType){

        if(!CollectionUtils.isEmpty(existingLeaveODRequestBeans)){

            if(existingLeaveODRequestBeans.size() == 2){
                throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
            }else{
                LeaveODRequestBean employeeODRequestBean = existingLeaveODRequestBeans.get(0);

                if(employeeODRequestBean.getFromDateType().equals(DayType.FULL_DAY)
                        || employeeODRequestBean.getFromDateType().equals(durationType)){
                    throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
                }
            }
        }
    }

    public void validateODUpdateRequest(LeaveODRequestDTO dto) {

        validateRequestDataDTO(dto);
        validateInvalidDayTypeRequest(dto);

        if (dto.getRequestId() == null || dto.getRequestId().trim().isEmpty()) {
            throw new ValidationException("Request Id is required");
        }

        if (dto.getFromDate() == null ) {
            throw new ValidationException("From date is required");
        }

        if (dto.getToDate() == null) {
            throw new ValidationException("To date is required");
        }

        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            throw new ValidationException("Reason is required");
        }

        if (dto.getTotalCount() == null || dto.getTotalCount() <= 0) {
            throw new ValidationException("Total count must be greater than zero");
        }
    }

}


