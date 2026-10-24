package com.smartcare.hrms.migration.service.matser.employeement;

import com.smartcare.hrms.common.entity.master.common.City;
import com.smartcare.hrms.common.entity.master.common.Country;
import com.smartcare.hrms.common.entity.master.common.State;
import com.smartcare.hrms.common.entity.master.employment.*;
import com.smartcare.hrms.common.repository.master.common.CityRepository;
import com.smartcare.hrms.common.repository.master.common.CountryRepository;
import com.smartcare.hrms.common.repository.master.common.StateRepository;
import com.smartcare.hrms.common.repository.master.employment.*;
import com.smartcare.hrms.migration.dto.master.employment.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class CombinedMasterUploadService {

    private @Autowired CategoryRepository categoryRepository;
    private @Autowired GradeRepository gradeRepository;
    private @Autowired DepartmentRepository departmentRepository;
    private @Autowired DesignationRepository designationRepository;
    private @Autowired WorkLocationRepository workLocationRepository;
    private @Autowired SubWorkLocationRepository   subWorkLocationRepository;
    private @Autowired BranchDetailsRepository     branchDetailsRepository;
    private @Autowired CompanyDetailsRepository    companyDetailsRepository;
    private @Autowired CountryRepository countryRepository;
    private @Autowired StateRepository stateRepository;
    private @Autowired CityRepository cityRepository;

    @Transactional
    public CombinedMasterUploadResultDTO uploadFromExcel(MultipartFile file) throws IOException {
        if (!CombinedMasterExcelHelper.isValidExcelFile(file)) {
            throw new IllegalArgumentException("Invalid file format. Please upload a .xlsx file.");
        }
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            return CombinedMasterUploadResultDTO.builder()
                    .categoryResult(processSimpleMaster(workbook, CombinedMasterExcelHelper.SHEET_CATEGORY, "Category",
                            code -> categoryRepository.existsByCode(code),
                            dto -> categoryRepository.save(Category.builder().name(dto.getName()).code(dto.getCode()).build())))
                    .gradeResult(processSimpleMaster(workbook, CombinedMasterExcelHelper.SHEET_GRADE, "Grade",
                            code -> gradeRepository.existsByCode(code),
                            dto -> gradeRepository.save(Grade.builder().name(dto.getName()).code(dto.getCode()).build())))
                    .departmentResult(processSimpleMaster(workbook, CombinedMasterExcelHelper.SHEET_DEPARTMENT, "Department",
                            code -> departmentRepository.existsByCode(code),
                            dto -> departmentRepository.save(Department.builder().name(dto.getName()).code(dto.getCode()).build())))
                    .designationResult(processSimpleMaster(workbook, CombinedMasterExcelHelper.SHEET_DESIGNATION, "Designation",
                            code -> designationRepository.existsByCode(code),
                            dto -> designationRepository.save(Designation.builder().name(dto.getName()).code(dto.getCode()).build())))
                    .workLocationResult(processSimpleMaster(workbook, CombinedMasterExcelHelper.SHEET_WORK_LOCATION, "WorkLocation",
                            code -> workLocationRepository.existsByCode(code),
                            dto -> workLocationRepository.save(WorkLocation.builder().name(dto.getName()).code(dto.getCode()).build())))
                    .subWorkLocationResult(processSubWorkLocation(workbook))
                    .branchResult(processBranch(workbook))
                    .companyResult(processCompany(workbook))
                    .build();
        }
    }

    // ─── Generic Simple Master Processor ─────────────────────────────────────────

    private MasterUploadResultDTO processSimpleMaster(
            Workbook workbook,
            String sheetName,
            String masterName,
            Predicate<String> existsByCode,
            Consumer<MasterUploadDTO> saveAction) {
        if (!sheetExists(workbook, sheetName)) {
            return MasterUploadResultDTO.builder()
                    .masterName(masterName)
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            MasterUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet '" + sheetName + "' not found")
                                    .build()
                    ))
                    .build();
        }
        List<MasterUploadDTO> list = CombinedMasterExcelHelper.parseSimple(workbook, sheetName);
        int success = 0, failure = 0;
        List<MasterUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            MasterUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName())) { errors.add(simpleError(row, dto, "Name is required")); failure++; continue; }
            if (isBlank(dto.getCode())) { errors.add(simpleError(row, dto, "Code is required")); failure++; continue; }
            if (existsByCode.test(dto.getCode())) { errors.add(simpleError(row, dto, "Code '" + dto.getCode() + "' already exists")); failure++; continue; }
            saveAction.accept(dto);
            success++;
        }

        return MasterUploadResultDTO.builder()
                .masterName(masterName)
                .totalRows(list.size())
                .successCount(success)
                .failureCount(failure)
                .errors(errors)
                .build();
    }

    // ─── SubWorkLocation ──────────────────────────────────────────────────────────

    private SubWorkLocationUploadResultDTO processSubWorkLocation(Workbook workbook) {

        if (!sheetExists(workbook, CombinedMasterExcelHelper.SHEET_SUB_WORK_LOCATION)) {
            return SubWorkLocationUploadResultDTO.builder()
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            SubWorkLocationUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<SubWorkLocationUploadDTO> list = CombinedMasterExcelHelper.parseSubWorkLocation(workbook);
        int success = 0, failure = 0;
        List<SubWorkLocationUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            SubWorkLocationUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName()))             { errors.add(subWLError(row, dto, " Sub Work Location  Name is required")); failure++; continue; }
            if (isBlank(dto.getCode()))             { errors.add(subWLError(row, dto, "Sub Work Location  Code is required")); failure++; continue; }
            if (isBlank(dto.getWorkLocationCode())) { errors.add(subWLError(row, dto, "Work Location Code is required")); failure++; continue; }
            if (subWorkLocationRepository.existsByCode(dto.getCode())) { errors.add(subWLError(row, dto, "Code '" + dto.getCode() + "' already exists")); failure++; continue; }

            Optional<WorkLocation> wlOpt = workLocationRepository.findByCodeIgnoreCase(dto.getWorkLocationCode());
            if (wlOpt.isEmpty()) { errors.add(subWLError(row, dto, "Work Location code '" + dto.getWorkLocationCode() + "' not found")); failure++; continue; }

            subWorkLocationRepository.save(SubWorkLocation.builder()
                    .name(dto.getName()).code(dto.getCode()).workLocation(wlOpt.get()).build());
            success++;
        }
        return SubWorkLocationUploadResultDTO.builder()
                .totalRows(list.size()).successCount(success).failureCount(failure).errors(errors).build();
    }

    // ─── Branch ───────────────────────────────────────────────────────────────────

    private BranchUploadResultDTO processBranch(Workbook workbook) {
        if (!sheetExists(workbook, CombinedMasterExcelHelper.SHEET_BRANCH)) {
            return BranchUploadResultDTO.builder()
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            BranchUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<BranchUploadDTO> list = CombinedMasterExcelHelper.parseBranch(workbook);
        int success = 0, failure = 0;
        List<BranchUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            BranchUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName()))        { errors.add(branchError(row, dto, "Branch Name is required")); failure++; continue; }
            if (isBlank(dto.getCode()))        { errors.add(branchError(row, dto, "Branch Code is required")); failure++; continue; }
            if (isBlank(dto.getCountryName())) { errors.add(branchError(row, dto, "Country Name is required")); failure++; continue; }
            if (isBlank(dto.getStateName()))   { errors.add(branchError(row, dto, "State Name is required")); failure++; continue; }
            if (isBlank(dto.getCityName()))    { errors.add(branchError(row, dto, "City Name is required")); failure++; continue; }
            if (branchDetailsRepository.existsByCode(dto.getCode())) { errors.add(branchError(row, dto, "Code '" + dto.getCode() + "' already exists")); failure++; continue; }

            Optional<Country> countryOpt = countryRepository.findByNameIgnoreCase(dto.getCountryName());
            if (countryOpt.isEmpty()) { errors.add(branchError(row, dto, "Country '" + dto.getCountryName() + "' not found")); failure++; continue; }

            Optional<State> stateOpt = stateRepository.findByNameIgnoreCaseAndCountryNameIgnoreCase(dto.getStateName(), dto.getCountryName());
            if (stateOpt.isEmpty()) { errors.add(branchError(row, dto, "State '" + dto.getStateName() + "' not found under '" + dto.getCountryName() + "'")); failure++; continue; }

            Optional<City> cityOpt = cityRepository.findByNameIgnoreCaseAndStateNameIgnoreCase(dto.getCityName(), dto.getStateName());
            if (cityOpt.isEmpty()) { errors.add(branchError(row, dto, "City '" + dto.getCityName() + "' not found under '" + dto.getStateName() + "'")); failure++; continue; }

            branchDetailsRepository.save(BranchDetails.builder()
                    .name(dto.getName()).code(dto.getCode()).address(dto.getAddress())
                    .countryId(countryOpt.get().getId().intValue())
                    .stateId(stateOpt.get().getId().intValue())
                    .cityId(cityOpt.get().getId().intValue())
                    .build());
            success++;
        }
        return BranchUploadResultDTO.builder()
                .totalRows(list.size()).successCount(success).failureCount(failure).errors(errors).build();
    }

    // ─── Company ──────────────────────────────────────────────────────────────────

    private CompanyUploadResultDTO processCompany(Workbook workbook) {
        if (!sheetExists(workbook, CombinedMasterExcelHelper.SHEET_COMPANY)) {
            return CompanyUploadResultDTO.builder()
                    .totalRows(0)
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(
                            CompanyUploadErrorDTO.builder()
                                    .rowNumber(0)
                                    .reason("Sheet not found")
                                    .build()
                    ))
                    .build();
        }
        List<CompanyUploadDTO> list = CombinedMasterExcelHelper.parseCompany(workbook);
        int success = 0, failure = 0;
        List<CompanyUploadErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            CompanyUploadDTO dto = list.get(i);
            int row = i + 2;
            if (isBlank(dto.getName()))        { errors.add(companyError(row, dto, "Company Name is required")); failure++; continue; }
            if (isBlank(dto.getCode()))        { errors.add(companyError(row, dto, "Company Code is required")); failure++; continue; }
            if (isBlank(dto.getCountryName())) { errors.add(companyError(row, dto, "Country Name is required")); failure++; continue; }
            if (isBlank(dto.getStateName()))   { errors.add(companyError(row, dto, "State Name is required")); failure++; continue; }
            if (isBlank(dto.getCityName()))    { errors.add(companyError(row, dto, "City Name is required")); failure++; continue; }
            if (companyDetailsRepository.existsByCode(dto.getCode())) { errors.add(companyError(row, dto, "Code '" + dto.getCode() + "' already exists")); failure++; continue; }

            Optional<Country> countryOpt = countryRepository.findByNameIgnoreCase(dto.getCountryName());
            if (countryOpt.isEmpty()) { errors.add(companyError(row, dto, "Country '" + dto.getCountryName() + "' not found")); failure++; continue; }

            Optional<State> stateOpt = stateRepository.findByNameIgnoreCaseAndCountryNameIgnoreCase(dto.getStateName(), dto.getCountryName());
            if (stateOpt.isEmpty()) { errors.add(companyError(row, dto, "State '" + dto.getStateName() + "' not found under '" + dto.getCountryName() + "'")); failure++; continue; }

            Optional<City> cityOpt = cityRepository.findByNameIgnoreCaseAndStateNameIgnoreCase(dto.getCityName(), dto.getStateName());
            if (cityOpt.isEmpty()) { errors.add(companyError(row, dto, "City '" + dto.getCityName() + "' not found under '" + dto.getStateName() + "'")); failure++; continue; }

            Designation designation = null;
            if (!isBlank(dto.getDesignationCode())) {
                designation = designationRepository.findByCode(dto.getDesignationCode());
                if (designation == null) { errors.add(companyError(row, dto, "Designation code '" + dto.getDesignationCode() + "' not found")); failure++; continue; }
            }

            LocalDate incorporationDate = null;
            if (!isBlank(dto.getIncorporationDate())) {
                try { incorporationDate = LocalDate.parse(dto.getIncorporationDate()); }
                catch (Exception e) { errors.add(companyError(row, dto, "Invalid Incorporation Date. Use YYYY-MM-DD")); failure++; continue; }
            }

            companyDetailsRepository.save(CompanyDetails.builder()
                    .name(dto.getName()).code(dto.getCode())
                    .registeredName(dto.getRegisteredName()).registeredNumber(dto.getRegisteredNumber())
                    .incorporationDate(incorporationDate).panNumber(dto.getPanNumber())
                    .tanNumber(dto.getTanNumber()).cinNumber(dto.getCinNumber())
                    .gstin(dto.getGstin()).ptRegNumber(dto.getPtRegNumber())
                    .pfRegistrationNumber(dto.getPfRegistrationNumber()).esicRegNumber(dto.getEsicRegNumber())
                    .msmeRegNo(dto.getMsmeRegNo()).iecCode(dto.getIecCode())
                    .ptZone(dto.getPtZone()).pfZone(dto.getPfZone()).esicZone(dto.getEsicZone())
                    .industryType(dto.getIndustryType()).address(dto.getAddress())
                    .countryId(countryOpt.get().getId().intValue())
                    .stateId(stateOpt.get().getId().intValue())
                    .cityId(cityOpt.get().getId().intValue())
                    .pincode(dto.getPincode()).contactPersonName(dto.getContactPersonName())
                    .designation(designation).contactPersonEmail(dto.getContactPersonEmail())
                    .countryCode(dto.getCountryCode()).phone(dto.getPhone()).email(dto.getEmail())
                    .website(dto.getWebsite()).twitter(dto.getTwitter())
                    .facebook(dto.getFacebook()).instagram(dto.getInstagram())
                    .build());
            success++;
        }
        return CompanyUploadResultDTO.builder()
                .totalRows(list.size()).successCount(success).failureCount(failure).errors(errors).build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private boolean isBlank(String value) { return value == null || value.isBlank(); }

    private MasterUploadErrorDTO simpleError(int row, MasterUploadDTO dto, String reason) {
        return MasterUploadErrorDTO.builder().rowNumber(row).name(dto.getName()).code(dto.getCode()).reason(reason).build();
    }

    private SubWorkLocationUploadErrorDTO subWLError(int row, SubWorkLocationUploadDTO dto, String reason) {
        return SubWorkLocationUploadErrorDTO.builder().rowNumber(row).name(dto.getName()).code(dto.getCode()).workLocationCode(dto.getWorkLocationCode()).reason(reason).build();
    }

    private BranchUploadErrorDTO branchError(int row, BranchUploadDTO dto, String reason) {
        return BranchUploadErrorDTO.builder().rowNumber(row).name(dto.getName()).code(dto.getCode())
                .address(dto.getAddress()).countryName(dto.getCountryName())
                .stateName(dto.getStateName()).cityName(dto.getCityName()).reason(reason).build();
    }

    private CompanyUploadErrorDTO companyError(int row, CompanyUploadDTO dto, String reason) {
        return CompanyUploadErrorDTO.builder().rowNumber(row).name(dto.getName()).code(dto.getCode())
                .countryName(dto.getCountryName()).stateName(dto.getStateName())
                .cityName(dto.getCityName()).designationCode(dto.getDesignationCode()).reason(reason).build();
    }

    private boolean sheetExists(Workbook workbook, String sheetName) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (workbook.getSheetAt(i).getSheetName().trim().equalsIgnoreCase(sheetName)) {
                return true;
            }
        }
        return false;
    }
}