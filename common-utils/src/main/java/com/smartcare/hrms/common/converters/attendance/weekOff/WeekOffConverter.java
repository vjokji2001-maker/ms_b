package com.smartcare.hrms.common.converters.attendance.weekOff;

import java.util.List;
import java.util.stream.Collectors;

import com.smartcare.hrms.common.dto.master.attendance.weekOff.WeekOffDetailDto;
import com.smartcare.hrms.common.dto.master.attendance.weekOff.WeekOffParentDto;
import com.smartcare.hrms.common.dto.master.attendance.weekOff.WeekOffTemplateDto;
import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffDetail;
import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffParent;
import com.smartcare.hrms.common.enumeration.DayOfWeek;
import com.smartcare.hrms.common.enumeration.WeekNo;
import com.smartcare.hrms.common.utility.EnumConverter;

public class WeekOffConverter {

	public static WeekOffParent toEntity(WeekOffParentDto dto) {
		 if (dto == null) {
			 return null;
		 }
		 
		 WeekOffParent entity = WeekOffParent.builder()
		 .name(dto.getName())
		 .code(dto.getCode())	 
		 .build(); 
		 
		 if (dto.getWeekOffDetail() != null) {
			List<WeekOffDetail> weekOff = dto.getWeekOffDetail().stream()
					.map(weekOffDto -> toEntity(weekOffDto, entity))
					.collect(Collectors.toList());
			entity.setWeekOffDetail(weekOff);
		}
		 return entity;
	}
	
	public static WeekOffDetail toEntity(WeekOffDetailDto dto, WeekOffParent weekOffParent) {
		if (dto == null) {
			 return null;
		 }
		
		return WeekOffDetail.builder()
				.weekOff(weekOffParent)
				.day(EnumConverter.toEnum(dto.getDay(), DayOfWeek.class))
				.weekNo(EnumConverter.toEnum(dto.getWeekNo(), WeekNo.class))
		        .build();	
		
	}
	
	
	public static WeekOffParentDto toDto(WeekOffParent entity) {
		if (entity == null) {
			return null;
		}
			
		WeekOffParentDto dto = WeekOffParentDto.builder()
				.name(entity.getName())
				.code(entity.getCode())
				.weekOffDetail(entity.getWeekOffDetail() == null ? null : entity.getWeekOffDetail().stream().map(WeekOffConverter::toDto).collect(Collectors.toList()))
				.build();
		dto.setId(entity.getId());
		return dto;
		
	}
	
	public static WeekOffDetailDto toDto(WeekOffDetail entity) {
		if (entity == null) {
			return null;
		}
		
		return WeekOffDetailDto.builder()
			   .id(entity.getId())
			   .day(EnumConverter.toDisplayName(entity.getDay()))
			   .weekNo(EnumConverter.toDisplayName(entity.getWeekNo()))
			   .build();
		
	}
	
	
	 public static WeekOffTemplateDto convertToDashboardDto(WeekOffParent weekOffParent) {
	        return WeekOffTemplateDto.builder()
	                .id(weekOffParent.getId())
	                .name(weekOffParent.getName())
	                .code(weekOffParent.getCode())
	                .build();
	    }
}
