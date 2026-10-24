package com.smartcare.hrms.payroll.service.statutory;

import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.payroll.dto.statutory.EmployeeStatutoryDTO;
import com.smartcare.hrms.common.entity.payroll.statutory.EmployeeStatutoryInfo;
import com.smartcare.hrms.payroll.mappers.compensation.employee.EmployeeStatutoryMapper;
import com.smartcare.hrms.common.repository.payroll.statutory.EmployeeStatutoryInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeStatutoryInfoService {

    private @Autowired EmployeeStatutoryInfoRepository employeeStatutoryInfoRepository;
    private @Autowired EmployeeStatutoryMapper employeeStatutoryMapper;
    private @Autowired EmployeeRepository employeeRepository;


    @Transactional
    public void create(EmployeeStatutoryDTO dto){
        EmployeeStatutoryInfo entity = employeeStatutoryMapper.toEmployeeStatutoryEntity(dto);
        if(dto.getEmpId() !=null){
            Employee employee = employeeRepository.findById(UUID.fromString(dto.getEmpId()))
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
            entity.setEmployee(employee);
        }
        employeeStatutoryInfoRepository.save(entity);

    }

    public EmployeeStatutoryDTO getById(String id){
        EmployeeStatutoryInfo info =employeeStatutoryInfoRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Employee statutory details not found"));
        return employeeStatutoryMapper.toDTO(info);
    }

    public List<EmployeeStatutoryDTO> getAll(){

        return employeeStatutoryInfoRepository.findAll()
                .stream()
                .map(employeeStatutoryMapper::toDTO)
                .toList();
    }

    @Transactional
    public EmployeeStatutoryDTO update(EmployeeStatutoryDTO dto){

        EmployeeStatutoryInfo info =employeeStatutoryInfoRepository.findById(UUID.fromString(dto.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Employee statutory details not found"));

        employeeStatutoryMapper.updateEmployeeStatutoryFromDto(dto, info);

        return employeeStatutoryMapper.toDTO(employeeStatutoryInfoRepository.save(info));


    }

    @Transactional
    public void delete(String id){
        EmployeeStatutoryInfo info = employeeStatutoryInfoRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Employee statutory details not found"));

        employeeStatutoryInfoRepository.delete(info);

    }
}
