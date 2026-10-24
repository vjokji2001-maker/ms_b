package com.smartcare.hrms.payroll.service.compensation.component;

import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.globalexception.ResourceConflictException;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.dto.payroll.compensation.component.ComponentDTO;
import com.smartcare.hrms.common.entity.payroll.compensation.component.ComponentMaster;
import com.smartcare.hrms.payroll.mappers.compensation.component.ComponentMasterMapper;
import com.smartcare.hrms.common.repository.payroll.compensation.component.ComponentMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComponentMasterService {

    private @Autowired ComponentMasterRepository repository;
    private @Autowired ComponentMasterMapper mapper;

    @Transactional
    public void create(ComponentDTO dto) {
        if (repository.existsByCode(dto.getCode())) {
            throw new ResourceConflictException("compensation.head.name.already.exists");
        }

        if (repository.existsByName(dto.getName().trim())) {
            throw new ResourceConflictException("compensation.head.code.already.exists");
        }

        ComponentMaster entity = mapper.toEntity(dto);
        repository.save(entity);
    }

    @Transactional
    public void update(ComponentDTO dto) {
        ComponentMaster existingEntity = repository.findById(dto.getId())
                .orElseThrow(() -> new ValidationException("compensation.head.not.found"));

        if (repository.existsByCodeAndIdNot(dto.getCode(), dto.getId())) {
            throw new ResourceConflictException("compensation.head.code.already.exists");
        }

        if (repository.existsByNameAndIdNot(dto.getName().trim(), dto.getId())) {
            throw new ResourceConflictException("compensation.head.name.already.exists");
        }

        existingEntity.setName(dto.getName());
        existingEntity.setCode(dto.getCode());
        existingEntity.setType(Enum.valueOf(ComponentType.class, dto.getType()));

        repository.save(existingEntity);
    }

    public ComponentDTO getById(Long id) {
        ComponentMaster entity = repository.findById(id)
                .orElseThrow(() -> new ValidationException("Compensation Head not found with id: " + id));
        return mapper.toDto(entity);
    }

    public ComponentDTO getByCode(String code) {
        ComponentMaster entity = repository.findByCode(code);
        return mapper.toDto(entity);
    }

    public List<ComponentDTO> getAll() {
        List<ComponentMaster> entities = repository.findAll();
        return entities.stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<ComponentDTO> getByIds(List<Long> ids) {
        List<ComponentMaster> entities = repository.findByIdIn(ids);
        return entities.stream()
                .map(mapper::toDto)
                .toList();
    }

}
