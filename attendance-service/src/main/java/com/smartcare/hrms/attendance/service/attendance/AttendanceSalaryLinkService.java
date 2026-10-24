package com.smartcare.hrms.attendance.service.attendance;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.AttendanceSalaryLinkConverter;
import com.smartcare.hrms.attendance.dto.attendance.AttendanceSalaryLinkDTO;
import com.smartcare.hrms.common.repository.attendance.AttendanceSalaryLinkRepository;
import com.smartcare.hrms.common.entity.attendance.AttendanceSalaryLink;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttendanceSalaryLinkService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private MessageResource messageResource;
    @Autowired
    private AttendanceSalaryLinkConverter salaryLinkConverter;
    @Autowired
    private AttendanceSalaryLinkRepository attendanceSalaryLinkRepository;


    public void create(AttendanceSalaryLinkDTO dto){

        if(ValidateUtils.isStringEmpty(dto.getEmployeeId())
                ||ValidateUtils.isNullObject(employeeRepository.getEmpId(dto.getEmployeeId()))){
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }
        if(ValidateUtils.isStringEmpty(dto.getMonthYear())){
            throw new ValidationException(messageResource.getEnglishMessage("month.year.required"));
        }

        AttendanceSalaryLink entity= salaryLinkConverter.convertDtoToEntity(dto);

        if(attendanceSalaryLinkRepository.existsByEmployeeIdAndMonthYear(
                entity.getEmployeeId(), entity.getMonthYear())){

            throw new ValidationException(messageResource.getEnglishMessage("salary.link.already.exist"));
        }
        attendanceSalaryLinkRepository.save(entity);


    }
}
