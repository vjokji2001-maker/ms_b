package com.smartcare.hrms.common.converters.attendance.leave;

import java.util.List;
import java.util.stream.Collectors;

import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveNameDTO;
import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveTemplateDTO;
import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveTemplateDashboardDTO;
import com.smartcare.hrms.common.dto.master.attendance.leave.LeaveTemplateDtlsDTO;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveName;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplate;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplateDtls;


public class LeaveConverter {

	public static LeaveTemplate toEntity(LeaveTemplateDTO dto) {
		if(dto==null) return null;
		LeaveTemplate entity= LeaveTemplate.builder()
				.name(dto.getName())
				.code(dto.getCode())
				.totalLeaveDays(dto.getTotalLeaveDays())
				.build();
		if(dto.getLeaveTemplateDtlsDTO()!=null) {
			List<LeaveTemplateDtls> leaveTemplate= dto.getLeaveTemplateDtlsDTO().stream()
					.map(leaveDto-> toEntity(leaveDto, entity)).collect(Collectors.toList());
			entity.setLeaveTemplateDtls(leaveTemplate);
		}
		return entity;
	}
	 public static LeaveTemplateDtls toEntity(LeaveTemplateDtlsDTO dto, LeaveTemplate leaveTemplate) {
	        if (dto == null) return null;

	        return LeaveTemplateDtls.builder()
	        		.leaveNameId(dto.getLeaveNameId())
	                .yearlyCount(dto.getYearlyCount())
	                .quaterlyCount(dto.getQuaterlyCount())
	                .monthlyCount(dto.getMonthlyCount())
	                .pastDays(dto.getPastDays())
	                .futureDays(dto.getFutureDays())
	                .halfDayAllowed(dto.isHalfDayAllowed())
	                .leave(leaveTemplate) 
	                .build();
	    }

	 public static LeaveTemplateDTO toDTO(LeaveTemplate leaveTemplate) {
		 if(leaveTemplate==null) return null;
		
		 LeaveTemplateDTO dto= LeaveTemplateDTO.builder()
				 .id(leaveTemplate.getId())
				 .name(leaveTemplate.getName())
				 .code(leaveTemplate.getCode())
				 .totalLeaveDays(leaveTemplate.getTotalLeaveDays())
				 .leaveTemplateDtlsDTO(
						 leaveTemplate.getLeaveTemplateDtls()==null?null:
							 leaveTemplate.getLeaveTemplateDtls().stream()
							 .map(LeaveConverter::toDTO)
							 .collect(Collectors.toList()))
				 .build();
		
		 return dto;
				
	 }
 
	 public static LeaveTemplateDtlsDTO toDTO(LeaveTemplateDtls leaveTemplateDtls) {
		 if(leaveTemplateDtls==null) return null;
		
		return  LeaveTemplateDtlsDTO.builder()
		 .id(leaveTemplateDtls.getId())
		 .leaveNameId(leaveTemplateDtls.getLeaveNameId())
		 .yearlyCount(leaveTemplateDtls.getYearlyCount())
		 .monthlyCount(leaveTemplateDtls.getMonthlyCount())
		 .quaterlyCount(leaveTemplateDtls.getQuaterlyCount())
		 .pastDays(leaveTemplateDtls.getPastDays() !=null?leaveTemplateDtls.getPastDays():0)
		 .futureDays(leaveTemplateDtls.getFutureDays() !=null?leaveTemplateDtls.getFutureDays():0)
		 .halfDayAllowed(leaveTemplateDtls.isHalfDayAllowed())
		 .build();
	 }
	

	 public static LeaveTemplateDashboardDTO toDashboardDTO(LeaveTemplate entity) {
	        LeaveTemplateDashboardDTO dto = new LeaveTemplateDashboardDTO();
	        dto.setId(entity.getId());
	        dto.setName(entity.getName());
	        dto.setCode(entity.getCode());
	        dto.setTotalLeaveDays(entity.getTotalLeaveDays());

	       
	        

	        return dto;
	    }
	 
	 public static LeaveName toEntity(LeaveNameDTO dto) {
		 if(dto==null) return null;
		 
		 LeaveName entity= LeaveName.builder()
				 .name(dto.getName())
				 .code(dto.getCode())
				 .colourCode(dto.getColourCode())
				 .build();
		 
		 return entity;
	 }
	 
	 public static LeaveNameDTO toDTO (LeaveName leaveName) {
		return LeaveNameDTO.builder()
				.id(leaveName.getId())
				.name(leaveName.getName())
				.code(leaveName.getCode())
				.colourCode(leaveName.getColourCode())
				.build();
	 }
}

