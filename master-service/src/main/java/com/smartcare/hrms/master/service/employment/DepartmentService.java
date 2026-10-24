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

import com.smartcare.hrms.common.dto.master.employment.DepartmentDto;
import com.smartcare.hrms.common.entity.master.employment.Department;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.employment.DepartmentRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;

@Service
public class DepartmentService {

	@Autowired
	private DepartmentRepository departmentRepository;

    @Autowired
    private MessageResource messageResource;

    /**
     * Creates a new department.
     *
     * @param departmentDto the department details to create
     * @return the created DepartmentDto
     */
    @Transactional
	public DepartmentDto create(DepartmentDto departmentDto) {

		// Validate department request
		validateDepartmentDto(departmentDto);
		validateByRepository(departmentDto);

        Department department = Department.builder()
                .name(departmentDto.getName())
                .code(departmentDto.getCode())
                .build();
		Department save = departmentRepository.save(department);	
		return toDto(save);
	}

    /**
     * Fetches departments by code, by name (containing), or all if no filters are provided.
     *
     * @param departmentDto the search criteria (code or name)
     * @return list of matching DepartmentDto objects
     */
	public List<DepartmentDto> getByNameOrCodeOrAll(DepartmentDto departmentDto) {
		List<DepartmentDto> dtoList = new ArrayList<>();
		if (!ValidateUtils.isStringEmpty(departmentDto.getName())) {
			List<Department> byName = departmentRepository.findByNameContainingIgnoreCase(departmentDto.getName());
			if (!CollectionUtils.isEmpty(byName)) {
				for (Department department : byName) {
					dtoList.add(toDto(department));
				}

			}
		} else if (!ValidateUtils.isStringEmpty(departmentDto.getCode())) {
			Department byCode = departmentRepository.findByCode(departmentDto.getCode());
			if (!ValidateUtils.isNullObject(byCode)) {
				dtoList.add(toDto(byCode));
			}
		} else {
			List<Department> all = departmentRepository.findAll();
			if (!CollectionUtils.isEmpty(all)) {
				for (Department department : all) {
					dtoList.add(toDto(department));
				}
			}
		}
		if (dtoList.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("department.data.not.found"));
		}
        dtoList.sort(Comparator.comparing(DepartmentDto::getName, String.CASE_INSENSITIVE_ORDER).reversed());
		return dtoList;
   }

    /**
     * Deletes a department by its ID.
     *
     * @param id the ID of the department to delete
     */
    @Transactional
	public void delete(Long id) {
		Department department = departmentRepository.findById(id)
		.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("department.data.not.found")));
		departmentRepository.delete(department);
	}

    /**
     * Updates an existing department.
     *
     * @param departmentDto the updated department details
     * @return the updated DepartmentDto
     */
    @Transactional
	public DepartmentDto update(DepartmentDto departmentDto) {
		if (ValidateUtils.isLongZero(departmentDto.getId())) {
			  throw	new ValidationException(messageResource.getEnglishMessage("department.details.required"));
			}
			validateDepartmentDto(departmentDto);

			Optional<Department> departmentOptional = departmentRepository.findById(departmentDto.getId());
			if (departmentOptional.isEmpty()) {
				throw new ValidationException(messageResource.getEnglishMessage("department.data.not.found"));
			}
	        Department department = departmentOptional.get();
			
			if (!department.getCode().equals(departmentDto.getCode())) {
				validateByRepository(departmentDto);
			}

			convertForUpdate(department, departmentDto);
			Department updated = departmentRepository.save(department);
			return toDto(updated);
		}


     /**
      * Retrieves a department by its ID.
      *
      * @param id the ID of the department
      * @return the corresponding DepartmentDto
      */
     public DepartmentDto getById(Long id) {
         Department department = departmentRepository.findById(id)
                 .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("department.data.not.found")));
         return toDto(department);
     }


    private DepartmentDto toDto(Department department) {
        if (department == null) {
            return null;
        }
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .build();
    }


    private void convertForUpdate(Department entity, DepartmentDto dto) {
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
    }


    private void validateDepartmentDto(DepartmentDto dto) {
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

    private void validateByRepository(DepartmentDto departmentDto) {
        Department byCode = departmentRepository.findByCode(departmentDto.getCode());
        if (!ValidateUtils.isNullObject(byCode)) {
            throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
        }
    }
}
