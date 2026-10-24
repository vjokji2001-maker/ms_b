package com.smartcare.hrms.common.dto.master.attendance.leave;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveTemplateDtlsDTO  {
	 
	    private Long id;
	    private Long leaveNameId;
		private Double yearlyCount;
		private Double quaterlyCount;
		private Double monthlyCount;
		private Double pastDays;
		private Double futureDays;
		private boolean halfDayAllowed;
	}