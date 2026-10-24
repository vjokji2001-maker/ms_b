package com.smartcare.hrms.attendance.validator;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.leave.CancelLeaveRequestDTO;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CancelODRequestValidator {
    private @Autowired MessageResource messageResource;

    private @Autowired EmployeeRepository employeeRepository;

    public void validateCancelODRequest(CancelLeaveRequestDTO requestDTO){

        if (ValidateUtils.isStringEmpty(requestDTO.getRequestId())) {
            throw new ValidationException(messageResource.getEnglishMessage("OD.request.not.found"));
        }

        if (ValidateUtils.isStringEmpty(requestDTO.getEmployeeId()) ||
                ValidateUtils.isNullObject(employeeRepository.getEmpId(requestDTO.getEmployeeId()))) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        if (ValidateUtils.isStringEmpty(requestDTO.getReason())) {
            throw new ValidationException(messageResource.getEnglishMessage("od.reason.required"));
        }
    }
}
