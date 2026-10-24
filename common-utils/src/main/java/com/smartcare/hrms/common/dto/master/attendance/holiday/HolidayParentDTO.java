package com.smartcare.hrms.common.dto.master.attendance.holiday;

import java.time.Year;
import java.util.List;

import com.smartcare.hrms.common.dto.BaseIdDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HolidayParentDTO extends BaseIdDTO {

	private String name;
	private String code;
	private Year year;
	private Double totalDays;
	private List<HolidayChildDTO> holidayChildDTO;


}
