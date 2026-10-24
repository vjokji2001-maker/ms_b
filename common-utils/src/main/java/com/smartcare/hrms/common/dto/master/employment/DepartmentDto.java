package com.smartcare.hrms.common.dto.master.employment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DepartmentDto{

	 private Long id; 
	 private String name;
	 private String code;
}
