package com.smartcare.hrms.attendance.validator;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveBalanceDTO;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.attendance.period.YearCycleRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LeaveBalanceValidator {

    private @Autowired EmployeeRepository employeeRepository;

    private @Autowired YearCycleRepository yearCycleRepository;

    private  @Autowired MessageResource messageResource;

    public void validateLeaveBalanceRequestData(LeaveBalanceDTO dto){

          if(ValidateUtils.isStringEmpty(dto.getEmployeeId()) ||
                ValidateUtils.isNullObject(employeeRepository.getEmpId(dto.getEmployeeId()))){
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }
        // year validation
        if(ValidateUtils.isLongZero(dto.getYearId())){
            throw new ValidationException(messageResource.getEnglishMessage("year.not.found"));
        }
    }
}
