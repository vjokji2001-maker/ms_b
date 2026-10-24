package com.smartcare.hrms.attendance.service.attendance;

import com.smartcare.hrms.common.bean.attendance.AttendanceDetailsBean;
import com.smartcare.hrms.common.bean.attendance.EmployeeAttendanceBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.AttendanceDetailsConverter;
import com.smartcare.hrms.attendance.converter.MyAttendanceConverter;
import com.smartcare.hrms.attendance.dto.attendance.AttendanceDetailsDTO;
import com.smartcare.hrms.attendance.dto.attendance.MyAttendanceDTO;
import com.smartcare.hrms.common.repository.attendance.EmployeeAttendanceRepository;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class MyAttendanceService {

    private @Autowired EmployeeAttendanceRepository attendanceRepository;
    private @Autowired MyAttendanceConverter attendanceConverter;
    private @Autowired AttendanceDetailsConverter attendanceDetailsConverter;
    private @Autowired MessageResource messageResource;


    public List<MyAttendanceDTO> getAttendanceByEmployeeAndMonth(String empId, Integer year, Integer month) {
        LocalDate fromDate = LocalDate.of(year, month, 1);
        LocalDate toDate = YearMonth.of(year, month).atEndOfMonth();

        List<EmployeeAttendanceBean> beanList = attendanceRepository.findByEmployeeIdAndDateRange(empId, fromDate, toDate);

        return attendanceConverter.convertBeanToDTO(beanList);
    }

    public AttendanceDetailsDTO getAttendanceDetails(String employeeId, LocalDate date) {
        if (ValidateUtils.isStringEmpty(employeeId)) {
            throw new ValidationException(messageResource.getEnglishMessage("attendance.employee.not.found"));
        }
        if (date == null) {
            throw new ValidationException(messageResource.getEnglishMessage("attendance.date.not.found"));
        }
        List<AttendanceDetailsBean> beans = attendanceRepository.getAttendanceDetails(employeeId, date);
        if (CollectionUtils.isEmpty(beans)) {
            throw new ValidationException(messageResource.getEnglishMessage("attendance.details.not.found"));
        }
        return attendanceDetailsConverter.convertBeanToDTO(beans);
    }

    @Transactional
    public int lockAttendance (String empId, List<String> dates){

      if(empId==null||dates==null||dates.isEmpty()){
          return 0;
      }
      int updateStatus= attendanceRepository.lockAttendance(empId,dates);

      return updateStatus;
    }
}