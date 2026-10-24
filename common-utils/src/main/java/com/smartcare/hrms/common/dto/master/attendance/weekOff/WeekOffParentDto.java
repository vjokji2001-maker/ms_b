package com.smartcare.hrms.common.dto.master.attendance.weekOff;

import java.util.List;

import com.smartcare.hrms.common.dto.BaseIdDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WeekOffParentDto extends BaseIdDTO{
	
	private String name;
    private String code;
    private List<WeekOffDetailDto> weekOffDetail;
}
