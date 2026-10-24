package com.smartcare.hrms.common.dto.master.attendance.weekOff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WeekOffTemplateDto {
	private Long id;
	private String name;
    private String code;
}
