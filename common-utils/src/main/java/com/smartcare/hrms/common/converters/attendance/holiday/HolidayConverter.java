package com.smartcare.hrms.common.converters.attendance.holiday;

import com.smartcare.hrms.common.dto.master.attendance.holiday.HolidayChildDTO;
import com.smartcare.hrms.common.dto.master.attendance.holiday.HolidayDashboardDTO;
import com.smartcare.hrms.common.dto.master.attendance.holiday.HolidayParentDTO;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayChild;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;
import com.smartcare.hrms.common.utility.DateHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class HolidayConverter {

	public static HolidayParent toEntity(HolidayParentDTO dto) {
		if(dto==null) return null;

		HolidayParent holidayParent= HolidayParent.builder()
				.name(dto.getName())
				.code(dto.getCode())
				.year(dto.getYear())
				.totalDays(dto.getTotalDays())
				.build();

		if(dto.getHolidayChildDTO()!=null) {
			List<HolidayChild> holidayChild= dto.getHolidayChildDTO().stream()
					.map(childDto->toEntity(childDto, holidayParent)).collect(Collectors.toList());
			holidayParent.setHolidayChild(holidayChild);
		}
		return holidayParent;
	}

	public static HolidayChild toEntity(HolidayChildDTO child, HolidayParent holidayParent) {
		if(child==null) return null;

	 return HolidayChild.builder()
				.name(child.getName())
				.date(DateHelper.convertUserDateToLocalDate(child.getDate()))
				.checked(child.isChecked())
				.holiday(holidayParent)
				.build();
	}



	public static HolidayParentDTO toDTO(HolidayParent entity) {
		if(entity==null) return null;

		HolidayParentDTO dto = HolidayParentDTO.builder()
				.name(entity.getName())
				.code(entity.getCode())
				.year(entity.getYear())
				.totalDays(entity.getTotalDays())
				.holidayChildDTO(
						entity.getHolidayChild()==null?null:
							entity.getHolidayChild().stream()
							.map(HolidayConverter::toDTO)
							.collect(Collectors.toList()))
							.build();
		dto.setId(entity.getId());

		return dto;
	}

	public static HolidayChildDTO toDTO(HolidayChild holidayChild) {
		if(holidayChild==null) return null;

		return HolidayChildDTO.builder()
				.id(holidayChild.getId())
				.name(holidayChild.getName())
				.date(String.valueOf(holidayChild.getDate()))
				.checked(holidayChild.isChecked())
				.build();
	}

	public static HolidayDashboardDTO toDashboardDTO(HolidayParent holidayParent) {
		return HolidayDashboardDTO.builder()
				.id(holidayParent.getId())
				.name(holidayParent.getName())
				.code(holidayParent.getCode())
				.year(holidayParent.getYear())
				.totalDays(holidayParent.getTotalDays())
				.build();
	}
}
