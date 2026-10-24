package com.smartcare.hrms.attendance.converter;

import com.smartcare.hrms.common.bean.attendance.EmployeeAttendanceBean;
import com.smartcare.hrms.attendance.dto.attendance.MyAttendanceDTO;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class MyAttendanceConverter {
    public List<MyAttendanceDTO> convertBeanToDTO(List<EmployeeAttendanceBean> beanList) {
        List<MyAttendanceDTO> dtoList = new ArrayList<>();

        if (beanList != null && !beanList.isEmpty()) {
            for (EmployeeAttendanceBean bean : beanList) {
                String totalHours = "00:00:00";
                if (bean.getEmpInTime() != null && bean.getEmpOutTime() != null) {
                    Duration duration = Duration.between(bean.getEmpInTime(), bean.getEmpOutTime());
                    if (duration.isNegative()) {
                        duration = duration.plusDays(1);
                    }
                    totalHours = String.format("%02d:%02d:%02d",
                            duration.toHours(),
                            duration.toMinutesPart(),
                            duration.toSecondsPart());
                }

                // ✅ null safe extra hours — use Duration instead of LocalTime.parse
                String extraHours = "00:00:00";
                if (bean.getTotalHours() != null && bean.getEmpInTime() != null && bean.getEmpOutTime() != null) {
                    try {
                        // parse shift hours (HH:mm:ss) safely
                        String[] parts = bean.getTotalHours().split(":");
                        long shiftSeconds = Long.parseLong(parts[0]) * 3600
                                + Long.parseLong(parts[1]) * 60
                                + Long.parseLong(parts[2]);

                        Duration shiftDuration = Duration.ofSeconds(shiftSeconds);

                        Duration workedDuration = Duration.between(bean.getEmpInTime(), bean.getEmpOutTime());
                        if (workedDuration.isNegative()) {
                            workedDuration = workedDuration.plusDays(1);
                        }

                        Duration diff = workedDuration.minus(shiftDuration);
                        if (!diff.isNegative()) {
                            extraHours = String.format("%02d:%02d:%02d",
                                    diff.toHours(),
                                    diff.toMinutesPart(),
                                    diff.toSecondsPart());
                        }
                    } catch (Exception e) {
                        extraHours = "00:00:00";
                    }
                }

                MyAttendanceDTO dto = new MyAttendanceDTO();
                dto.setEmployeeId(bean.getEmployeeId());
                dto.setDate(bean.getDate());
                dto.setDay(bean.getDay());
                dto.setShiftCode(bean.getShiftCode());
                dto.setShiftInTime(bean.getShiftInTime());
                dto.setShiftOutTime(bean.getShiftOutTime());
                dto.setEmpInTime(bean.getEmpInTime());
                dto.setEmpOutTime(bean.getEmpOutTime());
                dto.setDayType(bean.getDayType1());
                dto.setDayType1(bean.getDayType2());
                dto.setPortion(bean.getPortion());
                dto.setLateInMark(bean.getLateInMark() != null ? bean.getLateInMark() : 0.0);
                dto.setEarlyOutMark(bean.getEarlyOutMark() != null ? bean.getEarlyOutMark() : 0.0);
                dto.setTotalMark((bean.getLateInMark() != null ? bean.getLateInMark() : 0.0) + (bean.getEarlyOutMark() != null ? bean.getEarlyOutMark() : 0.0));
                dto.setTotalHours(totalHours);
                dto.setExtraHours(extraHours);
                dto.setStatus(String .valueOf(bean.getStatus()) );
                dtoList.add(dto);
            }
        }
        return dtoList;
    }



}
