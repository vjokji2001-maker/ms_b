package com.smartcare.hrms.common.dto.master.attendance.holiday;

import java.time.Year;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HolidayDashboardDTO {

	private Long id;
	private String name;
	private String code;
	private Year year;
	private Double totalDays;
}
