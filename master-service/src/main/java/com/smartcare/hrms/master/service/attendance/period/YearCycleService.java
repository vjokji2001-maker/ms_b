package com.smartcare.hrms.master.service.attendance.period;

import com.smartcare.hrms.common.dto.master.attendance.period.MonthCycleDto;
import com.smartcare.hrms.common.dto.master.attendance.period.YearCycleDto;
import com.smartcare.hrms.common.entity.master.attendance.period.YearCycle;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.attendance.period.YearCycleRepository;
import com.smartcare.hrms.common.repository.master.employment.GradeRepository;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import com.smartcare.hrms.master.config.MessageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class YearCycleService {

    @Autowired
    private YearCycleRepository yearCycleRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private MessageResource messageResource;



   /**
    *  Creates a new YearCycle
    *
    * @param yearCycleDto the DTO containing yearCycle details
    * @throws ValidationException if the provided data is invalid
    * */
    @Transactional
    public void create(YearCycleDto yearCycleDto){

        // Validate yearCycleDto request
        validateRequestDto(yearCycleDto);
        validateByRepository(yearCycleDto);
        YearCycle entity = toEntity(yearCycleDto);
        yearCycleRepository.save(entity);

    }

    /**
     * Updates an existing yearCycle entity
     *
     * @param yearCycleDto the DTO with updated data; must have a valid non-zero ID
     * @throws ValidationException if the ID is zero or null
     */
    @Transactional
    public void update(YearCycleDto yearCycleDto){
        if (ValidateUtils.isLongZero(yearCycleDto.getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("year.cycle.required"));
        }
        validateRequestDto(yearCycleDto);
        Optional<YearCycle> optionalyearCycle = yearCycleRepository.findById(yearCycleDto.getId());
        if (optionalyearCycle.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("year.cycle.data.not.found"));
        }
        YearCycle yearCycle = optionalyearCycle.get();

        validateByRepository(yearCycleDto);

        /** update entity with dto **/
        convertForUpdate(yearCycle, yearCycleDto);
        yearCycleRepository.save(yearCycle);
    }


    /**
     * Retrieves a year master by its ID.
     *
     * @param id the ID of the year master
     * @return the corresponding yearCycleDto
     */
    public YearCycleDto getById(Long id){
        Optional<YearCycle> optionalyearCycle = yearCycleRepository.findById(id);
        if (optionalyearCycle.isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("year.cycle.data.not.found"));
        }
        else {
           return toDto(optionalyearCycle.get());
        }
    }


    /**
     * Fetches year master by name (containing), by code or all if no filters are provided.
     *
     * @param yearCycleDto the search criteria (name or code)
     * @return list of matching yearCycleDTO objects
     */
    public List<YearCycleDto> getByNameOrCodeOrAll(YearCycleDto yearCycleDto) {
        List<YearCycleDto> dtoList = new ArrayList<>();
        if (!ValidateUtils.isStringEmpty(yearCycleDto.getName())) {
            List<YearCycle> byName = yearCycleRepository.findByNameContainingIgnoreCase(yearCycleDto.getName());
            if (!CollectionUtils.isEmpty(byName)) {
                for (YearCycle yearCycle : byName) {
                    dtoList.add(toDto(yearCycle));
                }
            }
        } else if (!ValidateUtils.isStringEmpty(yearCycleDto.getCode())) {
            YearCycle byCode = yearCycleRepository.findByCode(yearCycleDto.getCode());
            if (!ValidateUtils.isNullObject(byCode)) {
                dtoList.add(toDto(byCode));
            }
        } else {
            List<YearCycle> all = yearCycleRepository.findAll();
            if (!CollectionUtils.isEmpty(all)) {
                for (YearCycle yearCycle : all) {
                    dtoList.add(toDto(yearCycle));
                }
            }

        }
        if (dtoList.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("year.cycle.data.not.found"));
        }
        dtoList.sort(Comparator.comparing(YearCycleDto::getName,String.CASE_INSENSITIVE_ORDER).reversed());
        return dtoList;
    }

    /**
     * Deletes a year master by its ID.
     *
     * @param id the ID of the year master to delete
     */
    @Transactional
    public void delete(Long id) {
        YearCycle yearCycle = yearCycleRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("year.cycle.data.not.found")));
        yearCycleRepository.delete(yearCycle);
    }


    private YearCycle toEntity(YearCycleDto yearCycleDto){
        if (yearCycleDto == null){
            return null;
        }

        // Convert DTO string to LocalDate (first day of month)
        LocalDate periodStart  = DateHelper.toLocalDate(yearCycleDto.getFromMonthYear());

        // Calculate toMonthYear as last day of 12th month
        LocalDate periodEnd  = periodStart.plusMonths(11).with(TemporalAdjusters.lastDayOfMonth());

        return YearCycle.builder()
                .name(yearCycleDto.getName())
                .code(yearCycleDto.getCode())
                .fromMonthYear(periodStart)
                .toMonthYear(periodEnd)
                .build();

    }


    private void convertForUpdate(YearCycle yearCycle, YearCycleDto yearCycleDto){
        yearCycle.setName(yearCycleDto.getName());
        yearCycle.setCode(yearCycleDto.getCode());

        // Convert DTO string to LocalDate (first day of month)
        LocalDate periodStart  = DateHelper.toLocalDate(yearCycleDto.getFromMonthYear());

        // Calculate toMonthYear as last day of 12th month
        LocalDate periodEnd  = periodStart.plusMonths(11).with(TemporalAdjusters.lastDayOfMonth());

        yearCycle.setFromMonthYear(periodStart);
        yearCycle.setToMonthYear(periodEnd);

    }


    private YearCycleDto toDto(YearCycle yearCycle){
        if (yearCycle == null){
            return null;
        }
        return YearCycleDto.builder()
                .id(yearCycle.getId())
                .name(yearCycle.getName())
                .code(yearCycle.getCode())
                .fromMonthYear(DateHelper.toYearMonthString(yearCycle.getFromMonthYear()))
                .toMonthYear(DateHelper.toYearMonthString(yearCycle.getToMonthYear()))
                .build();

    }


    private void validateRequestDto(YearCycleDto yearCycleDto) {
        if (ValidateUtils.isNullObject(yearCycleDto)){
            throw new ValidationException(messageResource.getEnglishMessage("year.cycle.required"));
        }
        if (ValidateUtils.isStringEmpty(yearCycleDto.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("name.required"));
        }
        if (yearCycleDto.getName().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("name.too.long"));
        }
        if (!ValidateUtils.isValidNameFormat(yearCycleDto.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
        }
        if (ValidateUtils.isStringEmpty(yearCycleDto.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("code.required"));
        }
        if (yearCycleDto.getCode().length() > 10) {
            throw new ValidationException(messageResource.getEnglishMessage("code.too.long"));
        }
        if (!ValidateUtils.isValidCodeFormat(yearCycleDto.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
        }
        if (ValidateUtils.isStringEmpty(yearCycleDto.getFromMonthYear())){
            throw new ValidationException(messageResource.getEnglishMessage("from.month.year.required"));
        }
    }

    private void validateByRepository(YearCycleDto yearCycleDto) {
        YearCycle byCode = yearCycleRepository.findByCode(yearCycleDto.getCode());
        if (!ValidateUtils.isNullObject(byCode) && !byCode.getId().equals(yearCycleDto.getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
        }
        LocalDate periodStart  = DateHelper.toLocalDate(yearCycleDto.getFromMonthYear());
        LocalDate periodEnd  = periodStart.plusMonths(11).with(TemporalAdjusters.lastDayOfMonth());

        YearCycle byPeriod = yearCycleRepository.findByFromMonthYearAndToMonthYear(periodStart, periodEnd);

        if (!ValidateUtils.isNullObject(byPeriod) && !byPeriod.getId().equals(yearCycleDto.getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("year.cycle.period.exists"));
        }
    }
}
