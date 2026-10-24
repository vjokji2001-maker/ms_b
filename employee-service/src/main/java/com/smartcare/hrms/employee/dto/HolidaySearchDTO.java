package com.smartcare.hrms.employee.dto;

import com.smartcare.hrms.common.pagination.PaginationInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HolidaySearchDTO {

	private PaginationInfo paginationInfo;
	private String name;
	private String code;
	private Long companyId;
	private Long branchId;
	private Long designationId;
	private Long departmentId;
	private Long holidayId;
}
