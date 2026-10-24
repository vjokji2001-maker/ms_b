package com.smartcare.hrms.common.dto.master.attendance.holiday;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HolidayChildDTO {

	private Long id;
	private String name;
	private String date;
	private boolean checked;
}
