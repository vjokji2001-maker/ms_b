package com.smartcare.hrms.migration.service.employee;

import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.employee.EmployeeConfig;
import com.smartcare.hrms.common.entity.master.common.City;
import com.smartcare.hrms.common.entity.master.common.Country;
import com.smartcare.hrms.common.entity.master.common.State;
import com.smartcare.hrms.common.entity.master.employment.*;
import com.smartcare.hrms.common.enumeration.Gender;
import com.smartcare.hrms.common.enumeration.MaritalStatus;
import com.smartcare.hrms.common.enumeration.Salutation;
import com.smartcare.hrms.common.enumeration.Status;
import com.smartcare.hrms.common.repository.employee.EmployeeConfigRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.common.CityRepository;
import com.smartcare.hrms.common.repository.master.common.CountryRepository;
import com.smartcare.hrms.common.repository.master.common.StateRepository;
import com.smartcare.hrms.common.repository.master.employment.*;
import com.smartcare.hrms.migration.dto.employee.EmployeeUploadDTO;
import com.smartcare.hrms.migration.dto.employee.EmployeeUploadErrorDTO;
import com.smartcare.hrms.migration.dto.employee.EmployeeUploadResultDTO;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeExcelUploadService {


    private @Autowired EmployeeRepository        employeeRepository;
    private @Autowired CompanyDetailsRepository  companyDetailsRepository;
    private @Autowired BranchDetailsRepository   branchDetailsRepository;
    private @Autowired DesignationRepository     designationRepository;
    private @Autowired DepartmentRepository      departmentRepository;
    private @Autowired WorkLocationRepository    workLocationRepository;
    private @Autowired SubWorkLocationRepository subWorkLocationRepository;
    private @Autowired GradeRepository          gradeRepository;
    private @Autowired CategoryRepository        categoryRepository;
    private @Autowired CountryRepository         countryRepository;
    private @Autowired StateRepository           stateRepository;
    private @Autowired CityRepository            cityRepository;
    private @Autowired EmployeeConfigRepository  employeeConfigRepository;
    // ─── Upload Entry Point ───────────────────────────────────────────────────────

    @Transactional
    public EmployeeUploadResultDTO uploadFromExcel(MultipartFile file) throws IOException {
        if (!EmployeeExcelHelper.isValidExcelFile(file)) {
            throw new IllegalArgumentException("Invalid file format. Please upload a .xlsx file.");
        }
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            return processEmployees(workbook);
        }
    }

    // ─── Core Processor ───────────────────────────────────────────────────────────

    private EmployeeUploadResultDTO processEmployees(Workbook workbook) {
        if (!sheetExists(workbook, EmployeeExcelHelper.SHEET_EMPLOYEE)) {
            return EmployeeUploadResultDTO.builder()
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            EmployeeUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<EmployeeUploadDTO> list = EmployeeExcelHelper.parseEmployee(workbook);
        int success = 0, failure = 0;
        List<EmployeeUploadErrorDTO> errors = new ArrayList<>();
        List<Employee> savedEmployees = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            EmployeeUploadDTO dto = list.get(i);
            int row = i + 2;

            // ── Required field validation ─────────────────────────────────────────
            if (isBlank(dto.getFirstName()))   { errors.add(error(row, dto, "First Name is required"));    failure++; continue; }
            if (isBlank(dto.getLastName()))    { errors.add(error(row, dto, "Last Name is required"));     failure++; continue; }
            if (isBlank(dto.getGender()))      { errors.add(error(row, dto, "Gender is required"));        failure++; continue; }
            if (isBlank(dto.getDob()))         { errors.add(error(row, dto, "Date of Birth is required")); failure++; continue; }
            if (isBlank(dto.getEmpCode()))     { errors.add(error(row, dto, "Employee Code is required")); failure++; continue; }
            if (isBlank(dto.getJoiningDate())) { errors.add(error(row, dto, "Joining Date is required"));  failure++; continue; }
            if (isBlank(dto.getCompanyCode())) { errors.add(error(row, dto, "Company Code is required"));  failure++; continue; }
            if (isBlank(dto.getSalutation()))  { errors.add(error(row, dto, "Salutation is required"));    failure++; continue; }
            if (isBlank(dto.getStatus()))      { errors.add(error(row, dto, "Status is required"));        failure++; continue; }

            // ── Duplicate check ───────────────────────────────────────────────────
            if (employeeRepository.existsByEmpCode(dto.getEmpCode())) {
                errors.add(error(row, dto, "Employee Code '" + dto.getEmpCode() + "' already exists"));
                failure++; continue;
            }

            // ── Enum: Salutation ──────────────────────────────────────────────────
            Salutation salutation = parseSalutation(dto.getSalutation());
            if (salutation == null) {
                errors.add(error(row, dto, "Invalid Salutation '" + dto.getSalutation() + "'. Valid values: "
                        + Arrays.stream(Salutation.values()).map(Salutation::getDisplayName).toList()));
                failure++; continue;
            }

            // ── Enum: Gender ──────────────────────────────────────────────────────
            Gender gender = parseEnum(Gender.class, dto.getGender());
            if (gender == null) {
                errors.add(error(row, dto, "Invalid Gender '" + dto.getGender() + "'. Valid values: "
                        + Arrays.stream(Gender.values()).map(Enum::name).toList()));
                failure++; continue;
            }

            // ── Enum: MaritalStatus (optional) ────────────────────────────────────
            MaritalStatus maritalStatus = null;
            if (!isBlank(dto.getMaritalStatus())) {
                maritalStatus = parseMaritalStatus(dto.getMaritalStatus());
                if (maritalStatus == null) {
                    errors.add(error(row, dto, "Invalid Marital Status '" + dto.getMaritalStatus() + "'. Valid values: "
                            + Arrays.stream(MaritalStatus.values()).map(MaritalStatus::getDisplayName).toList()));
                    failure++; continue;
                }
            }

            // ── Enum: Status ──────────────────────────────────────────────────────
            Status status = parseEnum(Status.class, dto.getStatus());
            if (status == null) {
                errors.add(error(row, dto, "Invalid Status '" + dto.getStatus() + "'. Valid values: "
                        + Arrays.stream(Status.values()).map(Enum::name).toList()));
                failure++; continue;
            }

            // ── Date: DOB ─────────────────────────────────────────────────────────
            LocalDate dob;
            try { dob = LocalDate.parse(dto.getDob()); }
            catch (Exception e) { errors.add(error(row, dto, "Invalid Date of Birth. Use YYYY-MM-DD")); failure++; continue; }

            // ── Date: Joining Date ────────────────────────────────────────────────
            LocalDate joiningDate;
            try { joiningDate = LocalDate.parse(dto.getJoiningDate()); }
            catch (Exception e) { errors.add(error(row, dto, "Invalid Joining Date. Use YYYY-MM-DD")); failure++; continue; }

            // ── Date: Contract Start Date (optional) ──────────────────────────────
            LocalDate contractStartDate = null;
            if (!isBlank(dto.getContractStartDate())) {
                try { contractStartDate = LocalDate.parse(dto.getContractStartDate()); }
                catch (Exception e) { errors.add(error(row, dto, "Invalid Contract Start Date. Use YYYY-MM-DD")); failure++; continue; }
            }

            // ── Lookup: Company (required) ────────────────────────────────────────
            Optional<CompanyDetails> companyOpt = companyDetailsRepository.findByCodeIgnoreCase(dto.getCompanyCode());
            if (companyOpt.isEmpty()) { errors.add(error(row, dto, "Company '" + dto.getCompanyCode()+ "' not found")); failure++; continue; }

            // ── Lookup: Branch (optional) ─────────────────────────────────────────
            BranchDetails branch = null;
            if (!isBlank(dto.getBranchCode())) {
                Optional<BranchDetails> branchOpt = branchDetailsRepository.findByCodeIgnoreCase(dto.getBranchCode());
                if (branchOpt.isEmpty()) { errors.add(error(row, dto, "Branch '" + dto.getBranchCode() + "' not found")); failure++; continue; }
                branch = branchOpt.get();
            }

            // ── Lookup: Designation (optional) ────────────────────────────────────
            Designation designation = null;
            if (!isBlank(dto.getDesignationCode())) {
                designation = designationRepository.findByCodeIgnoreCase(dto.getDesignationCode());
                if (designation == null) { errors.add(error(row, dto, "Designation '" + dto.getDesignationCode() + "' not found")); failure++; continue; }
            }

            // ── Lookup: Department (optional) ─────────────────────────────────────
            Department department = null;
            if (!isBlank(dto.getDepartmentCode())) {
                department = departmentRepository.findByCodeIgnoreCase(dto.getDepartmentCode());
                if (department == null) { errors.add(error(row, dto, "Department '" + dto.getDepartmentCode() + "' not found")); failure++; continue; }
            }

            // ── Lookup: Work Location (optional) ──────────────────────────────────
            WorkLocation workLocation = null;
            if (!isBlank(dto.getWorkLocationCode())) {
                Optional<WorkLocation> wlOpt = workLocationRepository.findByCodeIgnoreCase(dto.getWorkLocationCode());
                if (wlOpt.isEmpty()) { errors.add(error(row, dto, "Work Location '" + dto.getWorkLocationCode() + "' not found")); failure++; continue; }
                workLocation = wlOpt.get();
            }

            // ── Lookup: Sub Work Location (optional) ──────────────────────────────
            SubWorkLocation subWorkLocation = null;
            if (!isBlank(dto.getSubWorkLocationCode())) {
                Optional<SubWorkLocation> swlOpt = subWorkLocationRepository.findByCodeIgnoreCase(dto.getSubWorkLocationCode());
                if (swlOpt.isEmpty()) { errors.add(error(row, dto, "Sub Work Location '" + dto.getSubWorkLocationCode() + "' not found")); failure++; continue; }
                subWorkLocation = swlOpt.get();
            }

            // ── Lookup: Grade (optional) ──────────────────────────────────────────
            Grade grade = null;
            if (!isBlank(dto.getGradeCode())) {
                grade = gradeRepository.findByCodeIgnoreCase(dto.getGradeCode());
                if (grade == null) { errors.add(error(row, dto, "Grade '" + dto.getGradeCode() + "' not found")); failure++; continue; }
            }

            // ── Lookup: Category (optional) ───────────────────────────────────────
            Category category = null;
            if (!isBlank(dto.getCategoryCode())) {
                category = categoryRepository.findByCodeIgnoreCase(dto.getCategoryCode());
                if (category == null) { errors.add(error(row, dto, "Category '" + dto.getCategoryCode() + "' not found")); failure++; continue; }
            }

            // ── Lookup: Country → Integer ID (optional) ───────────────────────────
            Integer birthCountryId = null;
            if (!isBlank(dto.getBirthCountryName())) {
                Optional<Country> countryOpt = countryRepository.findByNameIgnoreCase(dto.getBirthCountryName());
                if (countryOpt.isEmpty()) { errors.add(error(row, dto, "Country '" + dto.getBirthCountryName() + "' not found")); failure++; continue; }
                birthCountryId = countryOpt.get().getId().intValue();
            }

            // ── Lookup: State → Integer ID (optional) ─────────────────────────────
            Integer stateId = null;
            if (!isBlank(dto.getStateName())) {
                String countryName = isBlank(dto.getBirthCountryName()) ? "" : dto.getBirthCountryName();
                Optional<State> stateOpt = stateRepository.findByNameIgnoreCaseAndCountryNameIgnoreCase(dto.getStateName(), countryName);
                if (stateOpt.isEmpty()) { errors.add(error(row, dto, "State '" + dto.getStateName() + "' not found")); failure++; continue; }
                stateId = stateOpt.get().getId().intValue();
            }

            // ── Lookup: City → Integer ID (optional) ──────────────────────────────
            Integer cityId = null;
            if (!isBlank(dto.getCityName())) {
                String stateName = isBlank(dto.getStateName()) ? "" : dto.getStateName();
                Optional<City> cityOpt = cityRepository.findByNameIgnoreCaseAndStateNameIgnoreCase(dto.getCityName(), stateName);
                if (cityOpt.isEmpty()) { errors.add(error(row, dto, "City '" + dto.getCityName() + "' not found")); failure++; continue; }
                cityId = cityOpt.get().getId().intValue();
            }

            // ── Save ──────────────────────────────────────────────────────────────
            Employee saved = employeeRepository.save(Employee.builder()
                    .salutation(salutation)
                    .firstName(dto.getFirstName())
                    .middleName(dto.getMiddleName())
                    .lastName(dto.getLastName())
                    .formalName(dto.getFormalName())
                    .gender(gender)
                    .dob(dob)
                    .maritalStatus(maritalStatus)
                    .contactCountryCode(dto.getContactCountryCode())
                    .contactNumber(dto.getContactNumber())
                    .aadhaarNumber(dto.getAadhaarNumber())
                    .nationality(dto.getNationality())
                    .birthCountryId(birthCountryId)
                    .stateId(stateId)
                    .cityId(cityId)
                    .company(companyOpt.get())
                    .branch(branch)
                    .designation(designation)
                    .department(department)
                    .workLocation(workLocation)
                    .subWorkLocation(subWorkLocation)
                    .grade(grade)
                    .category(category)
                    .status(status)
                    .empCode(dto.getEmpCode())
                    .biometricCode(dto.getBiometricCode())
                    .joiningDate(joiningDate)
                    .contractStartDate(contractStartDate)
                    .smartCareUserCreated(false)
                    .build());
            savedEmployees.add(saved);
            success++;
        }

        if (!savedEmployees.isEmpty()) {
            List<EmployeeConfig> empConfigs = savedEmployees.stream()
                    .map(emp -> EmployeeConfig.builder()
                            .employee(emp)       // emp_id
                            .weekOffId(null)      // week_off_id  — set default if needed
                            .approvalId(null)// aprvl_grp_id — set default if needed
                            .holidayParent(null) // holiday_clndr_id
                            .build())
                    .toList();
            employeeConfigRepository.saveAll(empConfigs);
        }

        return EmployeeUploadResultDTO.builder()
                .totalRows(list.size())
                .successCount(success)
                .failureCount(failure)
                .errors(errors)
                .build();
    }

    // ─── Enum Parsers ─────────────────────────────────────────────────────────────

    /** Matches by enum name (case-insensitive). Used for Gender, Status, etc. */
    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
        if (isBlank(value)) return null;
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value.trim())) return constant;
        }
        return null;
    }

    /** Salutation — matches by displayName (e.g. "Mr.") or enum name (e.g. "MR"). */
    private Salutation parseSalutation(String value) {
        if (isBlank(value)) return null;
        String v = value.trim();
        for (Salutation s : Salutation.values()) {
            if (s.name().equalsIgnoreCase(v) || s.getDisplayName().equalsIgnoreCase(v)) return s;
        }
        return null;
    }

    /** MaritalStatus — matches by displayName (e.g. "Single") or enum name (e.g. "SINGLE"). */
    private MaritalStatus parseMaritalStatus(String value) {
        if (isBlank(value)) return null;
        String v = value.trim();
        for (MaritalStatus m : MaritalStatus.values()) {
            if (m.name().equalsIgnoreCase(v) || m.getDisplayName().equalsIgnoreCase(v)) return m;
        }
        return null;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private boolean isBlank(String value) { return value == null || value.isBlank(); }

    private EmployeeUploadErrorDTO error(int row, EmployeeUploadDTO dto, String reason) {
        return EmployeeUploadErrorDTO.builder()
                .rowNumber(row)
                .empCode(dto.getEmpCode())
                .firstName(dto.getFirstName())
                .reason(reason)
                .build();
    }

    private boolean sheetExists(Workbook workbook, String sheetName) {
        return workbook.getSheet(sheetName) != null;
    }
}