package com.smartcare.hrms.attendance.service.application.leave;


import com.smartcare.hrms.common.bean.application.leave.LeaveRequestBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.LeaveBalanceConverter;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveBalanceDTO;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveBalanceResponse;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveRequestRepository;
import com.smartcare.hrms.attendance.validator.LeaveBalanceValidator;
import com.smartcare.hrms.common.bean.attendance.EmployeeLeaveDataBean;
import com.smartcare.hrms.common.repository.employee.EmployeeLeaveParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveBalanceService {

    private @Autowired MessageResource messageResource;

    private @Autowired EmployeeLeaveParentRepository parentRepository;

    private @Autowired LeaveRequestRepository requestRepository;

    private @Autowired LeaveBalanceValidator leaveBalanceValidator;


    private @Autowired LeaveBalanceConverter leaveBalanceConverter;

    public List<LeaveBalanceResponse> get(LeaveBalanceDTO dto){

        leaveBalanceValidator.validateLeaveBalanceRequestData(dto);

        List<LeaveRequestBean> employeeLeaveRequestBean= requestRepository.getEmployeeLeaveRequestDataByEmpIdAndYearId(dto.getEmployeeId(),dto.getYearId());

        List<EmployeeLeaveDataBean> employeeLeaveDataBean=requestRepository.getLeaveDetailsByYearAndEmpId(dto.getEmployeeId(), dto.getYearId());

        return leaveBalanceConverter.calculateLeaveBalance(employeeLeaveRequestBean, employeeLeaveDataBean);

    }

}
