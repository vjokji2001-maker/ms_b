package com.smartcare.hrms.common.converters.attendance.shift;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.smartcare.hrms.common.dto.master.attendance.shift.ShiftLateMarkRuleDTO;
import com.smartcare.hrms.common.dto.master.attendance.shift.ShiftTemplateDTO;
import com.smartcare.hrms.common.dto.master.attendance.shift.ShiftTemplateDashboardDTO;
import com.smartcare.hrms.common.entity.master.attendance.shift.LateMarkRule;
import com.smartcare.hrms.common.entity.master.attendance.shift.ShiftTemplate;
import com.smartcare.hrms.common.enumeration.LateMarkType;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.TimeHelper;

public class ShiftConverter {

    public static ShiftTemplate toEntity(ShiftTemplateDTO dto) {
        if (dto == null) return null;

        ShiftTemplate entity = ShiftTemplate.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .nightShift(dto.isNightShift())
                .halfDay(dto.isHalfDay())
                .hourBased(dto.isHourBased())
                .inTime(TimeHelper.convertToLocalTime(dto.getInTime()))
                .outTime(TimeHelper.convertToLocalTime(dto.getOutTime()))
                .totalWorkingHours(TimeHelper.convertToLocalTime(dto.getTotalHours()))
                .firstHalfOutTime(TimeHelper.convertToLocalTime(dto.getFirstHalfOutTime()))
                .secondHalfInTime(TimeHelper.convertToLocalTime(dto.getSecondHalfInTime()))
                .totalHalfDayHours(dto.getTotalHalfDayHours() != null ? TimeHelper.convertToLocalTime(dto.getTotalHalfDayHours()) : null)
                .cutOffTime(dto.getCutOffTime())
                .bufferTime(dto.getBufferTime())
                .halfRate(dto.getHalfRate()!=null?dto.getHalfRate():0)
                .fullRate(dto.getFullRate()!=null?dto.getFullRate():0)
                .build();

        if (dto.getLateMarkRuleDTOList() != null) {
            List<LateMarkRule> rules = dto.getLateMarkRuleDTOList().stream()
                    .map(ruleDTO -> toEntity(ruleDTO, entity))
                    .collect(Collectors.toList());
            entity.setLateMarkRules(rules);
        }

        return entity;
    }

    public static LateMarkRule toEntity(ShiftLateMarkRuleDTO dto, ShiftTemplate shiftTemplate) {
        if (dto == null) return null;

        return LateMarkRule.builder()
                .fromTime(dto.getFromTime())
                .toTime(dto.getToTime())
                .markCount(dto.getMarkCount())
                .totalMarkCount(dto.getTotalMarkCount())
                .type(EnumConverter.toEnum(dto.getType(), LateMarkType.class))
                .shift(shiftTemplate)
                .build();
    }


    public static ShiftTemplateDTO toDTO(ShiftTemplate entity) {
        if (entity == null) return null;

        ShiftTemplateDTO dto = ShiftTemplateDTO.builder()
                .name(entity.getName())
                .code(entity.getCode())
                .nightShift(Boolean.TRUE.equals(entity.getNightShift()))
                .halfDay(Boolean.TRUE.equals(entity.getHalfDay()))
                .hourBased(Boolean.TRUE.equals(entity.getHourBased()))
                .inTime(TimeHelper.convertToString(entity.getInTime()))
                .outTime(TimeHelper.convertToString(entity.getOutTime()))
                .totalHours(TimeHelper.convertToString(calculateTotalHours(entity.getInTime(), entity.getOutTime())))
                .firstHalfOutTime(TimeHelper.convertToString(entity.getFirstHalfOutTime()))
                .secondHalfInTime(TimeHelper.convertToString(entity.getSecondHalfInTime()))
                .totalHalfDayHours(entity.getTotalHalfDayHours() != null ? TimeHelper.convertToString(entity.getTotalHalfDayHours()) : null)
                .cutOffTime(entity.getCutOffTime())
                .bufferTime(entity.getBufferTime())
                .halfRate(entity.getHalfRate()!=null?entity.getHalfRate():0)
                .fullRate(entity.getFullRate()!=null? entity.getFullRate() : 0)
                .lateMarkRuleDTOList(
                        entity.getLateMarkRules() == null ? null :
                                entity.getLateMarkRules().stream()
                                        .map(ShiftConverter::toDTO)
                                        .collect(Collectors.toList()))
                .build();
        dto.setId(entity.getId());
        return dto;
    }

    public static ShiftLateMarkRuleDTO toDTO(LateMarkRule rule) {
        if (rule == null) return null;

        return ShiftLateMarkRuleDTO.builder()
                .id(rule.getId())
                .fromTime(rule.getFromTime())
                .toTime(rule.getToTime())
                .markCount(rule.getMarkCount())
                .totalMarkCount(rule.getTotalMarkCount())
                .type(EnumConverter.toDisplayName(rule.getType()))
                .build();
    }

    // Helper: Calculate total hours from inTime and outTime
    private static LocalTime calculateTotalHours(LocalTime in, LocalTime out) {
        if (in == null || out == null) return null;

        int inMinutes = in.getHour() * 60 + in.getMinute();
        int outMinutes = out.getHour() * 60 + out.getMinute();

        // Handle night shift (crosses midnight)
        if (outMinutes < inMinutes) {
            outMinutes += 24 * 60;
        }

        int totalMinutes = outMinutes - inMinutes;

        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return LocalTime.of(hours, minutes);
    }


    public static ShiftTemplateDashboardDTO convertToDashboardDTO(ShiftTemplate shiftTemplate) {
        return ShiftTemplateDashboardDTO.builder()
                .id(shiftTemplate.getId())
                .name(shiftTemplate.getName())
                .code(shiftTemplate.getCode())
                .inTime(TimeHelper.convertToString(shiftTemplate.getInTime()))
                .outTime(TimeHelper.convertToString(shiftTemplate.getOutTime()))
                .totalWorkingHours(TimeHelper.convertToString(shiftTemplate.getTotalWorkingHours()))
                .nightShift(shiftTemplate.getNightShift())
                .halfDayApplicable(shiftTemplate.getHalfDay())
                .build();
    }
}
