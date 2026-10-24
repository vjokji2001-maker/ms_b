package com.smartcare.hrms.master.service.attendance.period;

import com.smartcare.hrms.common.dto.master.attendance.period.MonthCycleDto;
import com.smartcare.hrms.common.dto.master.attendance.shift.ShiftTemplateDashboardDTO;
import com.smartcare.hrms.master.config.MessageResource;
import com.smartcare.hrms.common.dto.master.employment.GradeDto;
import com.smartcare.hrms.common.entity.master.attendance.period.MonthCycle;
import com.smartcare.hrms.common.entity.master.employment.Grade;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.attendance.period.MonthCycleRepository;
import com.smartcare.hrms.common.repository.master.employment.GradeRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MonthCycleService {

    @Autowired
    private MonthCycleRepository monthCycleRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private MessageResource messageResource;



    /**
     *  Creates a new MonthCycle
     *
     * @param monthCycleDto the DTO containing MonthCycle details
     * @throws ValidationException if the provided data is invalid
     * */
    @Transactional
    public void create(MonthCycleDto monthCycleDto){

        // Validate yearCycleDto request
        validateRequestDto(monthCycleDto);
        validateByRepository(monthCycleDto);
        MonthCycle entity = toEntity(monthCycleDto);
        monthCycleRepository.save(entity);

    }


    /**
     * Updates an existing MonthlyCycle entity
     *
     * @param monthCycleDto the DTO with updated data; must have a valid non-zero ID
     * @throws ValidationException if the ID is zero or null
     */
    @Transactional
    public void update(MonthCycleDto monthCycleDto){
        if (ValidateUtils.isLongZero(monthCycleDto.getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("month.cycle.details.required"));
        }
        validateRequestDto(monthCycleDto);
        Optional<MonthCycle> optionalMonthCycle = monthCycleRepository.findById(monthCycleDto.getId());
        if (optionalMonthCycle.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("month.cycle.data.not.found"));
        }
        MonthCycle monthCycle = optionalMonthCycle.get();

        validateByRepository(monthCycleDto);

        /** update entity with dto **/
        convertForUpdate(monthCycle, monthCycleDto);
        monthCycleRepository.save(monthCycle);
    }


    /**
     * Retrieves a monthly cycle by its ID.
     *
     * @param id the ID of the monthly cycle
     * @return the corresponding monthlyCycleDto
     */
    public MonthCycleDto getById(Long id){
        Optional<MonthCycle> optionalMonthCycle = monthCycleRepository.findById(id);
        if (optionalMonthCycle.isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("month.cycle.data.not.found"));
        }
        else {
            return toDto(optionalMonthCycle.get());
        }
    }


    /**
     * Fetches monthly cycle by name (containing), by code or all if no filters are provided.
     *
     * @param monthCycleDto the search criteria (name or code)
     * @return list of matching monthlyCycleDto objects
     */
    public List<MonthCycleDto> getByNameOrCodeOrAll(MonthCycleDto monthCycleDto) {
        List<MonthCycleDto> dtoList = new ArrayList<>();
        if (!ValidateUtils.isStringEmpty(monthCycleDto.getName())) {
            List<MonthCycle> byName = monthCycleRepository.findByNameContainingIgnoreCase(monthCycleDto.getName());
            if (!CollectionUtils.isEmpty(byName)) {
                for (MonthCycle monthCycle : byName) {
                    dtoList.add(toDto(monthCycle));
                }
            }
        } else if (!ValidateUtils.isStringEmpty(monthCycleDto.getCode())) {
            MonthCycle byCode = monthCycleRepository.findByCode(monthCycleDto.getCode());
            if (!ValidateUtils.isNullObject(byCode)) {
                dtoList.add(toDto(byCode));
            }
        } else {
            List<MonthCycle> all = monthCycleRepository.findAll();
            if (!CollectionUtils.isEmpty(all)) {
                for (MonthCycle monthCycle : all) {
                    dtoList.add(toDto(monthCycle));
                }
            }
        }
        if (dtoList.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("month.cycle.data.not.found"));
        }
        dtoList.sort(Comparator.comparing(MonthCycleDto::getName,String.CASE_INSENSITIVE_ORDER).reversed());
        return dtoList;
    }

    /**
     * Deletes a monthly cycle by its ID.
     *
     * @param id the ID of the monthly cycle to delete
     */
    @Transactional
    public void delete(Long id) {
        MonthCycle monthCycle = monthCycleRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("month.cycle.data.not.found")));
        monthCycleRepository.delete(monthCycle);
    }



    private MonthCycle toEntity(MonthCycleDto monthCycleDto){
        if (monthCycleDto == null){
            return null;
        }

        // Get start date from user
        Integer cycleStart  = monthCycleDto.getStartDate();

        // Subtract (-) from start date to get end date
        Integer cycleEnd  = calculateEndDate(cycleStart);

        Grade grade = null;
        if (monthCycleDto.getGradeDto() != null && monthCycleDto.getGradeDto().getId() != null){
            grade = gradeRepository.findById(monthCycleDto.getGradeDto().getId())
                    .orElseThrow(()-> new ValidationException(messageResource.getEnglishMessage("grade.data.not.found")));

        }
        return MonthCycle.builder()
                .name(monthCycleDto.getName())
                .code(monthCycleDto.getCode())
                .startDate(cycleStart)
                .endDate(cycleEnd)
                .grade(grade)
                .build();

    }

    private void convertForUpdate(MonthCycle monthCycle, MonthCycleDto monthCycleDto){
        monthCycle.setName(monthCycleDto.getName());
        monthCycle.setCode(monthCycleDto.getCode());

        // Get start date from user
        Integer cycleStart  = monthCycleDto.getStartDate();

        // Subtract (-) from start date to get end date
        Integer cycleEnd  = calculateEndDate(cycleStart);

        monthCycle.setStartDate(cycleStart);
        monthCycle.setEndDate(cycleEnd);

        Grade grade = null;
        if (monthCycleDto.getGradeDto() != null && monthCycleDto.getGradeDto().getId() != null){
            grade = gradeRepository.findById(monthCycleDto.getGradeDto().getId())
                    .orElseThrow(()-> new ValidationException(messageResource.getEnglishMessage("grade.data.not.found")));

        }
        monthCycle.setGrade(grade);
    }


    private MonthCycleDto toDto(MonthCycle monthCycle){
        if (monthCycle == null){
            return null;
        }
        GradeDto gradeDto = null;
        if (monthCycle.getGrade() != null){
            Grade grade = monthCycle.getGrade();
            gradeDto = GradeDto.builder()
                    .id(grade.getId())
                    .name(grade.getName())
                    .code(grade.getCode())
                    .build();
        }
        return MonthCycleDto.builder()
                .id(monthCycle.getId())
                .name(monthCycle.getName())
                .code(monthCycle.getCode())
                .startDate(monthCycle.getStartDate())
                .endDate(monthCycle.getEndDate())
                .gradeDto(gradeDto)
                .build();
    }

    private Integer calculateEndDate(Integer startDate){
        return (startDate == 1 ) ? 30 : startDate - 1;
    }

    private void validateRequestDto(MonthCycleDto monthCycleDto) {
        if (ValidateUtils.isNullObject(monthCycleDto)){
            throw new ValidationException(messageResource.getEnglishMessage("month.cycle.details.required"));
        }
        if (ValidateUtils.isStringEmpty(monthCycleDto.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("name.required"));
        }
        if (monthCycleDto.getName().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("name.too.long"));
        }
        if (!ValidateUtils.isValidNameFormat(monthCycleDto.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
        }
        if (ValidateUtils.isStringEmpty(monthCycleDto.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("code.required"));
        }
        if (monthCycleDto.getCode().length() > 10) {
            throw new ValidationException(messageResource.getEnglishMessage("code.too.long"));
        }
        if (!ValidateUtils.isValidCodeFormat(monthCycleDto.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
        }
        if (ValidateUtils.isZero(monthCycleDto.getStartDate())){
            throw new ValidationException(messageResource.getEnglishMessage("start.date.required"));
        }
        if (monthCycleDto.getStartDate() < 1 || monthCycleDto.getStartDate() > 30){
            throw new ValidationException(messageResource.getEnglishMessage("invalid.start.date"));
        }
        if (ValidateUtils.isLongZero(monthCycleDto.getGradeDto().getId())){
            throw new ValidationException(messageResource.getEnglishMessage("grade.details.required"));
        }
    }


    private void validateByRepository(MonthCycleDto monthCycleDto) {
        MonthCycle byCode = monthCycleRepository.findByCode(monthCycleDto.getCode());
        if (!ValidateUtils.isNullObject(byCode) && (!byCode.getId().equals(monthCycleDto.getId()))) {
            throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
        }
        Integer cycleStart  = monthCycleDto.getStartDate();
        Integer cycleEnd  = (cycleStart == 1) ? 30 : cycleStart - 1;

        MonthCycle byStartDate = monthCycleRepository.findByStartDateAndEndDate(cycleStart, cycleEnd);
        if (!ValidateUtils.isNullObject(byStartDate) && (!byStartDate.getId().equals(monthCycleDto.getId()))){
            throw new ValidationException(messageResource.getEnglishMessage("month.cycle.exists"));
        }
    }
}
