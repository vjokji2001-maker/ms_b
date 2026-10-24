package com.smartcare.hrms.common.dto.master.attendance.meta;

import com.smartcare.hrms.common.dto.master.attendance.holiday.HolidayParentDTO;
import com.smartcare.hrms.common.dto.master.attendance.weekOff.WeekOffParentDto;
import com.smartcare.hrms.common.dto.master.employment.meta.MetaEmploymentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MetaAttendanceDTO {
    private MetaEmploymentDTO employmentData;
    private List<WeekOffParentDto> weekOffParentList;
    private List<HolidayParentDTO> holidayParentList;
}
