package com.smartcare.hrms.staff.service;

import com.smartcare.hrms.common.bean.employee.EmployeeBean;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.staff.config.MessageResource;
import com.smartcare.hrms.staff.dto.StaffProfileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class StaffProfileService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MessageResource messageResource;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public StaffProfileDTO getStaffProfile(String empId) {
        if (ValidateUtils.isStringEmpty(empId)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        Employee employee = employeeRepository.findById(UUID.fromString(empId))
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        StaffProfileDTO.StaffProfileDTOBuilder builder = StaffProfileDTO.builder()
                .empId(employee.getId().toString())
                .empCode(employee.getEmpCode())
                .formalName(employee.getFormalName() != null ? employee.getFormalName() : (employee.getFirstName() + " " + employee.getLastName()))
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .formalEmail(employee.getFormalEmail())
                .personalEmail(employee.getPersonalEmail())
                .contactNumber(employee.getContactNumber())
                .gender(employee.getGender() != null ? employee.getGender().name() : null)
                .dob(employee.getDob() != null ? employee.getDob().format(DATE_FORMATTER) : null)
                .joiningDate(employee.getJoiningDate() != null ? employee.getJoiningDate().format(DATE_FORMATTER) : null)
                .bloodGroup(employee.getBloodGroup() != null ? employee.getBloodGroup().name() : null)
                .status(employee.getStatus() != null ? employee.getStatus().name() : "ACTIVE");

        if (employee.getCompany() != null) {
            builder.companyName(employee.getCompany().getName());
        }
        if (employee.getBranch() != null) {
            builder.branchName(employee.getBranch().getName());
        }
        if (employee.getDepartment() != null) {
            builder.departmentName(employee.getDepartment().getName());
        }
        if (employee.getDesignation() != null) {
            builder.designationName(employee.getDesignation().getName());
        }
        if (employee.getWorkLocation() != null) {
            builder.workLocationName(employee.getWorkLocation().getName());
        }
        if (employee.getGrade() != null) {
            builder.gradeName(employee.getGrade().getName());
        }
        if (employee.getCategory() != null) {
            builder.categoryName(employee.getCategory().getName());
        }

        return builder.build();
    }
}
