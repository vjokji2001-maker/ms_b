package com.smartcare.hrms.common.dto.master.attendance.leave;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveTemplateDTO {

	private Long id;
	private String name;
	private String code;
	private Double totalLeaveDays;
	private List<LeaveTemplateDtlsDTO> leaveTemplateDtlsDTO;
}