package com.smartcare.hrms.attendance.service.shift;

import com.netflix.discovery.converters.Auto;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.converter.ShiftConverter;
import com.smartcare.hrms.attendance.dao.shift.EmployeeShiftDashboardDAO;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDTO;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDashboardDTO;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftSearchDTO;
import com.smartcare.hrms.common.bean.attendance.EmployeeShiftDetailBean;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.master.attendance.shift.ShiftTemplate;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.repository.attendance.shift.EmployeeShiftRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.attendance.shift.ShiftTemplateRepository;
import com.smartcare.hrms.common.utility.ValidateUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class EmployeeShiftService {


    @PersistenceContext
    private EntityManager entityManager;
    private @Autowired EmployeeShiftRepository employeeShiftRepository;
    private @Autowired MessageResource messageResource;
    private @Autowired EmployeeRepository employeeRepository;
    private @Autowired ShiftTemplateRepository shiftTemplateRepository;
    private @Autowired EmployeeShiftDashboardDAO employeeShiftDashboardDAO;

    public PaginatedDataResponse<EmployeeShiftDashboardDTO> getDashboardData(EmployeeShiftSearchDTO dto){
        if (ValidateUtils.isZero(dto.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        return employeeShiftDashboardDAO.getData(dto);
    }


    @Transactional
    public void update(List<EmployeeShiftDTO> dtoList) {

        validateRequest(dtoList);

        Set<UUID> empIds = new HashSet<>();
        Set<LocalDate> dates = new HashSet<>();
        Set<Long> shiftIds = new HashSet<>();

        for (EmployeeShiftDTO dto : dtoList) {
            empIds.add(UUID.fromString(dto.getEmpId()));
            dates.add(LocalDate.parse(dto.getDate()));
            shiftIds.add(dto.getShiftId());
        }

        List<EmployeeShift> existingShifts =
                employeeShiftRepository.findAllByEmployeeIdInAndDateIn(
                        new ArrayList<>(empIds),
                        new ArrayList<>(dates)
                );

        // Map for quick lookup (empId + date)
        Map<String, EmployeeShift> existingShiftMap = new HashMap<>();
        for (EmployeeShift es : existingShifts) {
            String key = es.getEmployee().getId() + "_" + es.getDate();
            existingShiftMap.put(key, es);
        }

        List<EmployeeShiftDetailBean> shiftDetailsList =
                shiftTemplateRepository.findAllShiftDataByIds(new ArrayList<>(shiftIds));

        Map<Long, EmployeeShiftDetailBean> shiftMap = new HashMap<>();
        for (EmployeeShiftDetailBean sd : shiftDetailsList) {
            shiftMap.put(sd.getShiftId(), sd);
        }

        List<EmployeeShift> saveList = new ArrayList<>();

        for (EmployeeShiftDTO dto : dtoList) {

            UUID employeeId = UUID.fromString(dto.getEmpId());
            LocalDate date = LocalDate.parse(dto.getDate());

            String key = employeeId + "_" + date;

            EmployeeShiftDetailBean shiftDetailBean = shiftMap.get(dto.getShiftId());
            if (shiftDetailBean == null) {
                throw new ValidationException(
                        messageResource.getEnglishMessage("shift.not.found"));
            }

            EmployeeShift employeeShift;

            if (existingShiftMap.containsKey(key)) {
                employeeShift = existingShiftMap.get(key);
                ShiftConverter.convertForUpdate(employeeShift, dto, shiftDetailBean);

            } else {
                employeeShift = new EmployeeShift();
                ShiftConverter.convertForCreate(employeeShift, dto, shiftDetailBean);
            }

            saveList.add(employeeShift);
        }

        employeeShiftRepository.saveAll(saveList);
    }

    private void validateRequest(List<EmployeeShiftDTO> dtoList){
        if(ValidateUtils.isNullObject(dtoList) || dtoList.isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("employee.shift.details.required"));
        }

        for(EmployeeShiftDTO dto : dtoList){
            validateSingleRequest(dto);
        }
    }

    private void validateSingleRequest(EmployeeShiftDTO dto){
        if(ValidateUtils.isNullObject(dto)){
            throw new ValidationException(messageResource.getEnglishMessage("employee.shift.details.required"));
        }

        if(dto.getEmpId()==null || dto.getEmpId().isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("employee.details.required"));
        }

        UUID employeeId = UUID.fromString(dto.getEmpId());
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));

        if(dto.getShiftId()==null || dto.getShiftId()==0){
            throw new ValidationException(messageResource.getEnglishMessage("shift.details.required"));
        }

        shiftTemplateRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("shift.not.found")));

        if(ValidateUtils.isStringEmpty(dto.getDate())){
            throw new ValidationException(messageResource.getEnglishMessage("date.required"));
        }
    }




}
