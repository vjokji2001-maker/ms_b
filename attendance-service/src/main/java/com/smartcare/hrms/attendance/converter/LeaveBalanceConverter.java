package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.common.bean.application.leave.LeaveRequestBean;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveBalanceResponse;
import com.smartcare.hrms.common.bean.attendance.EmployeeLeaveDataBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LeaveBalanceConverter {

    public List<LeaveBalanceResponse> calculateLeaveBalance(List<LeaveRequestBean> leaveRequests,
                                                            List<EmployeeLeaveDataBean> employeeLeaveData){

        Map<Long, List<LeaveRequestBean>> requestByLeaveNameId= leaveRequests.stream().collect(Collectors.groupingBy(LeaveRequestBean ::getLeaveNameId));

        List<LeaveBalanceResponse> balanceResponse= new ArrayList<>();

        for(EmployeeLeaveDataBean leaveData:employeeLeaveData){
            Long leaveNameId =leaveData.getLeaveNameId();
            String leaveName= leaveData.getLeaveName();


            List<LeaveRequestBean> request= requestByLeaveNameId.getOrDefault(leaveNameId, new ArrayList<>());

            LeaveBalanceResponse balance= new LeaveBalanceResponse();
            balance.setName(leaveName);
            balance.setLeaveNameId(leaveNameId);
            balance.setAccrued(leaveData.getYearlyCount().doubleValue());


            Double approvedCount= request.stream().filter(req -> "APPROVED".equalsIgnoreCase(req.getStatus()))
                    .mapToDouble(LeaveRequestBean::getTotalLeaveCount).sum();

            balance.setApproved(approvedCount);

            Double requestCount= request.stream().filter(req ->"PENDING".equalsIgnoreCase(req.getStatus()))
                    .mapToDouble(LeaveRequestBean::getTotalLeaveCount).sum();

            balance.setRequested(requestCount);

            double remainingBalance= leaveData.getYearlyCount()-approvedCount-requestCount;

            balance.setBalance(remainingBalance);

            balanceResponse.add(balance);

        }
        return balanceResponse;

    }
}
