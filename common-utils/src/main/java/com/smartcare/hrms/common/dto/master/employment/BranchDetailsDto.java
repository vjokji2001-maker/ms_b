package com.smartcare.hrms.common.dto.master.employment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BranchDetailsDto {
	
	 private Long id;
	 private String name;
	 private String code;
	 private String address;
     private Integer countryId;
	 private String countryName;
	 private Integer stateId;
	 private String stateName;
	 private Integer cityId;
	 private String cityName;

}
