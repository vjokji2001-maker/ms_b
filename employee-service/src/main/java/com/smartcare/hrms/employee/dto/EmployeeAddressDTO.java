package com.smartcare.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmployeeAddressDTO {

	private Long id;
    private Integer countryId;
	private String countryName;
	private Integer stateId;
	private String stateName;
	private Integer cityId;
	private String cityName;
	private String address;
	private String pinCode;
    private String addressType;
	private String fromDate;
	private String toDate;

}
