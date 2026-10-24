package com.smartcare.hrms.master.service.attendance.meta;

import com.smartcare.hrms.common.dto.master.attendance.holiday.HolidayParentDTO;
import com.smartcare.hrms.common.dto.master.attendance.meta.MetaAttendanceDTO;
import com.smartcare.hrms.common.dto.master.attendance.weekOff.WeekOffParentDto;
import com.smartcare.hrms.common.dto.master.employment.meta.MetaEmploymentDTO;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;
import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffParent;
import com.smartcare.hrms.common.repository.master.attendance.holiday.HolidayParentRepository;
import com.smartcare.hrms.common.repository.master.attendance.weekOff.WeekOffParentRepository;
import com.smartcare.hrms.master.service.employment.meta.MetaEmploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class MetaAttendanceService {

    private @Autowired MetaEmploymentService metaEmploymentService;
    private @Autowired WeekOffParentRepository weekOffParentRepository;
    private @Autowired HolidayParentRepository holidayParentRepository;

    public MetaAttendanceDTO getEmployeementAndAttendanceData(){
        MetaAttendanceDTO metaAttendanceDTO = new MetaAttendanceDTO();

        // Populate Employment Master Data
        MetaEmploymentDTO metaEmploymentData = metaEmploymentService.getAllEmploymentMasterData();
        metaAttendanceDTO.setEmploymentData(metaEmploymentData);

        // Populate WeekOff Parent List
        metaAttendanceDTO.setWeekOffParentList(
                weekOffParentRepository.findAll().stream()
                        .map(this::toWeekDTO)
                        .collect(Collectors.toList())
        );

        // Populate Holiday Parent List
        metaAttendanceDTO.setHolidayParentList(
                holidayParentRepository.findAll().stream()
                        .map(this::toHolidayDTO)
                        .collect(Collectors.toList())
        );

        return metaAttendanceDTO;
    }

    private WeekOffParentDto toWeekDTO(WeekOffParent entity){
        if(entity== null) return null;
        WeekOffParentDto dto = new WeekOffParentDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        return dto;
    }
    private HolidayParentDTO toHolidayDTO(HolidayParent entity){
        if(entity==null) return null;
        HolidayParentDTO dto = new HolidayParentDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setYear(entity.getYear());
        dto.setTotalDays(entity.getTotalDays());
        return dto;
    }
}
