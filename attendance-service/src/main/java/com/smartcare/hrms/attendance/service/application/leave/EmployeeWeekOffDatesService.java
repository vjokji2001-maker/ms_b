package com.smartcare.hrms.attendance.service.application.leave;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.WeekOffDateConverter;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.bean.employee.EmployeeMonthCycleBean;
import com.smartcare.hrms.common.bean.employee.EmployeeWeekOffBean;
import com.smartcare.hrms.common.entity.master.attendance.period.YearCycle;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.attendance.period.YearCycleRepository;
import com.smartcare.hrms.common.utility.DateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmployeeWeekOffDatesService {

    private @Autowired MessageResource messageResource;

    private @Autowired EmployeeRepository employeeRepository;

    private @Autowired YearCycleRepository yearCycleRepository;

    @Autowired
    private WeekOffDateConverter weekOffDateConverter;

    public List<String> getWeekOffDates(LeaveODRequestDTO requestDTO) {

        YearCycle yearCycle = yearCycleRepository.findById(requestDTO.getYearId())
                .orElseThrow(() ->
                        new ValidationException(messageResource.getEnglishMessage("year.not.found")));

        EmployeeMonthCycleBean employeeMonthCycle =
                employeeRepository.getEmployeeMonthCycle(requestDTO.getEmpId());

        Map<String, LocalDate> yearStartEndDateMap =
                DateHelper.getYearStartEndDate(
                        yearCycle.getFromMonthYear(),
                        employeeMonthCycle.getStartDate());

        LocalDate yearStartDate = yearStartEndDateMap.get("yearStartDate");
        LocalDate yearEndDate = yearStartEndDateMap.get("yearEndDate");

        List<EmployeeWeekOffBean> weekOffRules =
                employeeRepository.getEmployeeWeekDetail(requestDTO.getEmpId());

        return weekOffDateConverter.convert(
                weekOffRules,
                yearStartDate,
                yearEndDate
        );
    }
}

