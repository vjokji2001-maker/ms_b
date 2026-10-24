package com.smartcare.hrms.common.dto.master.attendance.leave;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveNameDTO {

	private Long id;
	private String name;
	private String code;
	private String colourCode;
}
