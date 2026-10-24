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

import com.smartcare.hrms.common.dto.master.employment.CategoryDto;
import com.smartcare.hrms.common.entity.master.employment.Category;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.master.employment.CategoryRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

    @Autowired
    private MessageResource messageResource;

    /**
     * Creates a new category.
     *
     * @param categoryDto the category details to create
     * @return the created CategoryDto
     */
    @Transactional
	public CategoryDto create(CategoryDto categoryDto) {

		// Validate category request
		validateCategoryDto(categoryDto);
		validateByRepository(categoryDto);

		Category category = Category.builder()
                .name(categoryDto.getName())
                .code(categoryDto.getCode())
                .build();

		Category save = categoryRepository.save(category);
		return toDto(save);

	}

    /**
     * Fetches categories by code, by name (containing), or all if no filters are provided.
     *
     * @param categoryDto the search criteria (code or name)
     * @return list of matching CategoryDto objects
     */
	public List<CategoryDto> getByNameOrCodeOrAll(CategoryDto categoryDto) {
		List<CategoryDto> dtoList = new ArrayList<>();
		if (!ValidateUtils.isStringEmpty(categoryDto.getName())) {
			List<Category> byName = categoryRepository.findByNameContainingIgnoreCase(categoryDto.getName());
			if (!CollectionUtils.isEmpty(byName)) {
				for (Category category : byName) {
					dtoList.add(toDto(category));
				}

			}
		} else if (!ValidateUtils.isStringEmpty(categoryDto.getCode())) {
			Category byCode = categoryRepository.findByCode(categoryDto.getCode());
			if (!ValidateUtils.isNullObject(byCode)) {
				dtoList.add(toDto(byCode));
			}
		} else {
			List<Category> all = categoryRepository.findAll();
			if (!CollectionUtils.isEmpty(all)) {
				for (Category category : all) {
					dtoList.add(toDto(category));
				}
			}
		}
		if (dtoList.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("category.data.not.found"));
		}
        dtoList.sort(Comparator.comparing(CategoryDto::getName, String.CASE_INSENSITIVE_ORDER).reversed());
		return dtoList;

	}

    /**
     * Deletes a category by its ID.
     *
     * @param id the ID of the category to delete
     */
    @Transactional
	public void delete(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("category.data.not.found")));
		categoryRepository.delete(category);

	}

    /**
     * Updates an existing category.
     *
     * @param categoryDto the updated category details
     * @return the updated CategoryDto
     */
    @Transactional
	public CategoryDto update(CategoryDto categoryDto) {
		if (ValidateUtils.isLongZero(categoryDto.getId())) {
			throw new ValidationException(messageResource.getEnglishMessage("category.details.required"));
		}
		validateCategoryDto(categoryDto);

		Optional<Category> categoryOptional = categoryRepository.findById(categoryDto.getId());
		if (categoryOptional.isEmpty()) {
			throw new ValidationException(messageResource.getEnglishMessage("category.data.not.found"));
		}
		Category category = categoryOptional.get();

		if (!category.getCode().equals(categoryDto.getCode())) {
			validateByRepository(categoryDto);
		}

		convertForUpdate(category, categoryDto);
		Category updated = categoryRepository.save(category);
		return toDto(updated);
	}


    /**
     * Retrieves a category by its ID.
     *
     * @param id the ID of the category
     * @return the corresponding CategoryDto
     */
    public CategoryDto getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("category.data.not.found")));
        return toDto(category);
    }


	private CategoryDto toDto(Category category) {
		if (category == null) {
			return null;
		}
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .code(category.getCode())
                .build();
	}

	private void convertForUpdate(Category entity, CategoryDto dto) {
		entity.setName(dto.getName());
		entity.setCode(dto.getCode());
	}

	private void validateCategoryDto(CategoryDto dto) {
		if (ValidateUtils.isStringEmpty(dto.getName())) {
			throw new ValidationException(messageResource.getEnglishMessage("name.required"));
		}
		if (dto.getName().length() > 255) {
			throw new ValidationException(messageResource.getEnglishMessage("name.too.long"));
		}
        if (!ValidateUtils.isValidNameFormat(dto.getName())){
            throw new ValidationException(messageResource.getEnglishMessage("invalid.name.format"));
        }
		if (ValidateUtils.isStringEmpty(dto.getCode())) {
			throw new ValidationException(messageResource.getEnglishMessage("code.required"));
		}
		if (dto.getCode().length() > 10) {
			throw new ValidationException(messageResource.getEnglishMessage("code.too.long"));
		}
        if (!ValidateUtils.isValidCodeFormat(dto.getCode())){
            throw new ValidationException(messageResource.getEnglishMessage("invalid.code.format"));
        }
	}

	private void validateByRepository(CategoryDto categoryDto) {
		Category byCode = categoryRepository.findByCode(categoryDto.getCode());
		if (!ValidateUtils.isNullObject(byCode)) {
			throw new ValidationException(messageResource.getEnglishMessage("code.exist"));
		}
	}
}
