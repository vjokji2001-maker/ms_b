package com.smartcare.hrms.common.dto.master.employment.meta;


import com.smartcare.hrms.common.dto.master.employment.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MetaEmploymentDTO {

   private List<DesignationDto> designationList;
   private List<DepartmentDto> departmentList;
   private List<CategoryDto> categoryList;
   private List<GradeDto> gradeList;
   private List<WorkLocationDto> workLocationList;
   private List<SubWorkLocationDto> subWorkLocationList;
   private List<BranchDetailsDto> branchDetailsList;
   private List<CompanyDetailsDto> companyDetailsList;

}
