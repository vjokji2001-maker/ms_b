package com.smartcare.hrms.master.service.employment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.smartcare.hrms.master.config.MessageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.smartcare.hrms.common.dto.master.employment.GradeDto;
import com.smartcare.hrms.common.entity.master.employment.Grade;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.employment.GradeRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private MessageResource messageResource;

    /**
     * Creates a new grade.
     *
     * @param gradeDto the grade details to create
     * @return the created GradeDto
     */
    @Transactional
    public GradeDto create(GradeDto gradeDto) {

        // Validate grade request
        validateGradeDto(gradeDto);
        validateByRepository(gradeDto);

        Grade grade = Grade.builder()
                .name(gradeDto.getName())
                .code(gradeDto.getCode())
                .build();

        Grade save = gradeRepository.save(grade);
        return toDto(save);
    }


    /**
     * Fetches grades by code, by name (containing), or all if no filters are provided.
     *
     * @param gradeDto the search criteria (code or name)
     * @return list of matching GradeDto objects
     */
    public List<GradeDto> getByNameOrCodeOrAll(GradeDto gradeDto) {
        List<GradeDto> dtoList = new ArrayList<>();
        if (!ValidateUtils.isStringEmpty(gradeDto.getName())) {
            List<Grade> byName = gradeRepository.findByNameContainingIgnoreCase(gradeDto.getName());
            if (!CollectionUtils.isEmpty(byName)) {
                for (Grade grade : byName) {
                    dtoList.add(toDto(grade));
                }

            }
        } else if (!ValidateUtils.isStringEmpty(gradeDto.getCode())) {
            Grade byCode = gradeRepository.findByCode(gradeDto.getCode());
            if (!ValidateUtils.isNullObject(byCode)) {
                dtoList.add(toDto(byCode));
            }
        } else {
            List<Grade> all = gradeRepository.findAll();
            if (!CollectionUtils.isEmpty(all)) {
                for (Grade grade : all) {
                    dtoList.add(toDto(grade));
                }
            }
        }
        if (dtoList.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("grade.data.not.found"));
        }
        dtoList.sort(Comparator.comparing(GradeDto::getName, String.CASE_INSENSITIVE_ORDER).reversed());
        return dtoList;
    }


    /**
     * Deletes an entry by its ID.
     *
     * @param id the ID of the entry to delete
     */
    @Transactional
    public void delete(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("grade.data.not.found")));
        gradeRepository.delete(grade);

    }

    /**
     * Updates an existing grade.
     *
     * @param gradeDto the updated grade details
     * @return the updated GradeDto
     */
    @Transactional
    public GradeDto update(GradeDto gradeDto) {
        if (ValidateUtils.isLongZero(gradeDto.getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("grade.details.required"));
        }
        validateGradeDto(gradeDto);

        Optional<Grade> gradeOptional = gradeRepository.findById(gradeDto.getId());
        if (gradeOptional.isEmpty()) {
            throw new ValidationException(messageResource.getEnglishMessage("grade.data.not.found"));
        }
        Grade grade = gradeOptional.get();

        if (!grade.getCode().equals(gradeDto.getCode())) {
            validateByRepository(gradeDto);
        }

        convertForUpdate(grade, gradeDto);
        Grade updated = gradeRepository.save(grade);
        return toDto(updated);
    }


    /**
     * Retrieves a grade by its ID.
     *
     * @param id the ID of the grade
     * @return the corresponding GradeDto
     */
    public GradeDto getById(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("grade.data.not.found")));
        return toDto(grade);
    }


    private GradeDto toDto(Grade grade) {
        if (grade == null) {
            return null;
        }
        return GradeDto.builder()
                .id(grade.getId())
                .name(grade.getName())
                .code(grade.getCode())
                .build();
    }


    private void convertForUpdate(Grade entity, GradeDto dto) {
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
    }


    private void validateGradeDto(GradeDto dto) {
        if (ValidateUtils.isStringEmpty(dto.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("name.required"));
        }
        if (dto.getName().length() > 255) {
            throw new ValidationException(messageResource.getEnglishMessage("name.too.long"));
        }
        if (!ValidateUtils.isValidNameFormat(dto.getName())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
        }
        if (ValidateUtils.isStringEmpty(dto.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("code.required"));
        }
        if (dto.getCode().length() > 10) {
            throw new ValidationException(messageResource.getEnglishMessage("code.too.long"));
        }
        if (!ValidateUtils.isValidCodeFormat(dto.getCode())) {
            throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
        }
    }


    private void validateByRepository(GradeDto gradeDto) {
        Grade byCode = gradeRepository.findByCode(gradeDto.getCode());
        if (!ValidateUtils.isNullObject(byCode)) {
            throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
        }
    }

}
