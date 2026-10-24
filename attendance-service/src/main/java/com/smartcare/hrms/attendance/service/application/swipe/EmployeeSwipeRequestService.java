package com.smartcare.hrms.attendance.service.application.swipe;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dao.application.swipe.EmployeeSwipeRequestDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.swipe.*;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDTO;
import com.smartcare.hrms.common.bean.attendance.EmployeeShiftDetailBean;
import com.smartcare.hrms.common.bean.attendance.EmployeeSwipeRequestBean;
import com.smartcare.hrms.common.bean.attendance.SwipeRequestApproverDetailsBean;
import com.smartcare.hrms.common.bean.master.attendance.ApprovalGroupBean;
import com.smartcare.hrms.common.entity.attendance.application.swipe.EmployeeSwipeRequest;
import com.smartcare.hrms.common.entity.attendance.application.swipe.SwipeRequestApproverDetails;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.enumeration.SwipeDirection;
import com.smartcare.hrms.common.enumeration.SwipeStatus;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.repository.attendance.application.swipe.EmployeeSwipeRequestRepository;
import com.smartcare.hrms.common.repository.attendance.shift.EmployeeShiftRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeConfigRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.TimeHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeSwipeRequestService {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private EmployeeSwipeRequestDashboardDAO employeeSwipeRequestDashboardDAO;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeSwipeRequestRepository employeeSwipeRequestRepository;

    @Autowired
    private EmployeeShiftRepository employeeShiftRepository;

    @Autowired
    private EmployeeConfigRepository employeeConfigRepository;


    /**
     *
     * @param searchDTO
     * @return Paginated response details
     */
    public PaginatedDataResponse<EmployeeSwipeRequestDashboardDTO> getPaginatedSwipeRequestList(EmployeeSwipeRequestSearchDTO searchDTO) {
        if (ValidateUtils.isZero(searchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        return employeeSwipeRequestDashboardDAO.getData(searchDTO);
    }

    /**
     * Creates a new swipe request for an employee and links it to the specified clinic.
     *
     * @param requestDTO the swipe request details
     * @param clinicId the clinic ID associated with the request
     */
    @Transactional
    public void create(EmployeeSwipeRequestDTO requestDTO, String clinicId, String empId){
        /* Validate employee swipe request */
        validateRequest(requestDTO);

        // Convert DTO to entity
        EmployeeSwipeRequest employeeSwipeRequest = toEntity(requestDTO, clinicId, empId);
        employeeSwipeRequestRepository.save(employeeSwipeRequest);
    }

    /**
     * Updates an existing employee swipe request with the provided details.
     * Validates the request, fetches the existing record, and applies changes.
     *
     * @param requestDTO the updated swipe request details
     * @param clinicId   the clinic ID associated with the current user or operation
     */
    @Transactional
    public void update(EmployeeSwipeRequestDTO requestDTO, String clinicId, String empId){
        if (ValidateUtils.isNullObject(requestDTO.getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.details.required"));
        }

        // Basic DTO field validations
        validateUpdateRequest(requestDTO);

        if (ValidateUtils.isStringEmpty(empId)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }

        EmployeeSwipeRequest employeeSwipeRequest = employeeSwipeRequestRepository.findById(requestDTO.getId())
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.swipe.record.not.found")));


        // prevent modification if already approved
        boolean hasApproved = employeeSwipeRequest.getApproverDetailsList() != null && employeeSwipeRequest.getApproverDetailsList().stream()
                             .anyMatch(approver -> approver.getSwipeStatus() == SwipeStatus.APPROVED);

        if (hasApproved) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.already.approved"));
        }

        EmployeeShift employeeShift = null;
        if (!ValidateUtils.isNullObject(requestDTO.getEmployeeShiftDTO()) && !ValidateUtils.isLongZero(requestDTO.getEmployeeShiftDTO().getId())){
            employeeShift = employeeShiftRepository.findById(requestDTO.getEmployeeShiftDTO().getId())
                    .orElseThrow(()-> new ValidationException(messageResource.getEnglishMessage("employee.shift.not.found")));
        }

        /** update entity with dto **/
        convertForUpdate(employeeSwipeRequest, requestDTO, employeeShift, clinicId, empId);
        employeeSwipeRequestRepository.save(employeeSwipeRequest);
    }

    /**
     * Deletes an employee swipe request by ID.
     * Allows deletion only for pending or rejected requests.
     *
     * @param id the ID of the swipe request to delete
     */
   @Transactional
    public void delete(UUID id){
        EmployeeSwipeRequest employeeSwipeRequest = employeeSwipeRequestRepository.findById(id)
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.swipe.record.not.found")));

       SwipeStatus status = employeeSwipeRequest.getSwipeStatus();

       // If already approved, do not allow deletion
       if (status == SwipeStatus.APPROVED) {
           throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.cannot.delete.approved"));
       }
       // If In-Progress, do not allow deletion
       if (status == SwipeStatus.IN_PROGRESS){
           throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.cannot.delete.in.progress"));
       }

       employeeSwipeRequestRepository.delete(employeeSwipeRequest);
   }

    /**
     * Retrieves an Employee Swipe Request by its unique ID.
     *
     * @param id the unique identifier of the swipe request
     * @return the {@link EmployeeSwipeRequestDTO} corresponding to the given ID
     */
    public EmployeeSwipeRequestDTO getById(String id){
        if (ValidateUtils.isStringEmpty(id)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.record.not.found"));
        }
        List<EmployeeSwipeRequestBean> swipeRequestBeans = employeeSwipeRequestRepository.getAllId(id);
        if (CollectionUtils.isEmpty(swipeRequestBeans)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.record.not.found"));
        }
        return convertBeanToDto(swipeRequestBeans);
    }

    /**
     * Retrieves the employee shift details for a specific date and employee.
     *
     * @param date the date (in DD-MM-YYYY format) to fetch the employee shift for
     * @param empId the employee ID to fetch the shift for
     * @return the employee shift details for the given date and employee
     */
    public EmployeeShiftDetailDTO getEmpShiftByDate(String date, String empId){
        if (ValidateUtils.isStringEmpty(date)){
            throw new ValidationException(messageResource.getEnglishMessage("employee.shift.date.required"));
        }
        if (ValidateUtils.isStringEmpty(empId)){
            throw new ValidationException(messageResource.getEnglishMessage("employee.id.required"));
        }
        EmployeeShiftDetailBean shiftDetailBean = employeeSwipeRequestRepository.getEmpShiftByDate(DateHelper.convertUserDateToLocalDate(date), empId);
        if (ValidateUtils.isNullObject(shiftDetailBean)){
            throw new ValidationException(messageResource.getEnglishMessage("employee.shift.not.found"));
        }
        return convertEmpShiftBeanToDto(shiftDetailBean);
    }



    /**
     * Validates the fields of the given EmployeeSwipRequestDTO.
     *
     * @param requestDto the EmployeeDTO to validate
     * @throws ValidationException if any required field is invalid or missing
     */
    private void validateRequest(EmployeeSwipeRequestDTO requestDto) {
        if (ValidateUtils.isNullObject(requestDto)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.request.null"));
        }
        if (ValidateUtils.isStringEmpty(requestDto.getSwipeDate())) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.date.required"));
        }
        if (!ValidateUtils.isStringEmpty(requestDto.getSwipeDate())) {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate swipeDate = LocalDate.parse(requestDto.getSwipeDate(), inputFormatter);
            if (swipeDate.isAfter(LocalDate.now())) {
                throw new ValidationException(messageResource.getEnglishMessage("swipe.future.date.not.allowed"));
            }
        }
        if (ValidateUtils.isStringEmpty(requestDto.getDayType())) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.day.type.required"));
        }
        if (ValidateUtils.isStringEmpty(requestDto.getSwipeDirection())) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.direction.required"));
        }
        if ( ValidateUtils.isNullObject(requestDto.getEmployeeShiftDTO()) || ValidateUtils.isLongZero(requestDto.getEmployeeShiftDTO().getId())){
            throw new ValidationException(messageResource.getEnglishMessage("employee.shift.required"));
        }
        String direction = requestDto.getSwipeDirection();
        if ("IN".equalsIgnoreCase(direction)) {
            if (ValidateUtils.isStringEmpty(requestDto.getReqInTime())) {
                throw new ValidationException(messageResource.getEnglishMessage("swipe.in.time.required"));
            }
        }
        else if ("OUT".equalsIgnoreCase(direction)) {
            if (ValidateUtils.isStringEmpty(requestDto.getReqOutTime())) {
                throw new ValidationException(messageResource.getEnglishMessage("swipe.out.time.required"));
            }
        }
        else if ("BOTH".equalsIgnoreCase(direction)) {
            if (ValidateUtils.isStringEmpty(requestDto.getReqInTime())) {
                throw new ValidationException(messageResource.getEnglishMessage("swipe.in.time.required"));
            }
            if (ValidateUtils.isStringEmpty(requestDto.getReqOutTime())) {
                throw new ValidationException(messageResource.getEnglishMessage("swipe.out.time.required"));
            }
        }
        if (ValidateUtils.isStringEmpty(requestDto.getRemark())){
            throw new ValidationException(messageResource.getEnglishMessage("swipe.request.remark.required"));
        }
    }

    private void validateUpdateRequest(EmployeeSwipeRequestDTO requestDto) {
        if (ValidateUtils.isNullObject(requestDto)) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.request.null"));
        }
        if (ValidateUtils.isNullObject(requestDto.getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.details.required"));
        }
        if (ValidateUtils.isStringEmpty(requestDto.getDayType())) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.day.type.required"));
        }
        if (ValidateUtils.isStringEmpty(requestDto.getSwipeDirection())) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.direction.required"));
        }
        if (ValidateUtils.isNullObject(requestDto.getEmployeeShiftDTO()) ||
                ValidateUtils.isLongZero(requestDto.getEmployeeShiftDTO().getId())) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.shift.required"));
        }
        if (ValidateUtils.isStringEmpty(requestDto.getReqInTime())) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.in.time.required"));
        }
        if (!"IN".equalsIgnoreCase(requestDto.getSwipeDirection()) && ValidateUtils.isStringEmpty(requestDto.getReqOutTime())) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.out.time.required"));
        }
        if (ValidateUtils.isStringEmpty(requestDto.getRemark())) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.request.remark.required"));
        }
    }


    private EmployeeSwipeRequest toEntity(EmployeeSwipeRequestDTO requestDTO, String clinicId, String empId){
        if (requestDTO == null) {
            return null;
        }

        // 1. Validate swipe date is not null
        if (ValidateUtils.isStringEmpty(requestDTO.getSwipeDate())) {
            throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.date.required"));
        }

        LocalDate swipeDate = DateHelper.convertUserDateToLocalDate(requestDTO.getSwipeDate());
//        LocalDate currentDate = LocalDate.now();

        // 2. Validation: Swipe date cannot be present or future
//        if (!swipeDate.isBefore(currentDate)) {
//            throw new ValidationException(messageResource.getEnglishMessage("employee.swipe.future.or.present.date.not.allowed"));
//        }

        // 3. Fetch employee
        Employee employee = null;
        if (!ValidateUtils.isStringEmpty(empId)){
            employee = employeeRepository.findById(UUID.fromString(empId))
                      .orElseThrow(()-> new ValidationException(messageResource.getEnglishMessage("employee.not.found")));
        }

        // 4. Fetch employee shift
        EmployeeShift employeeShift = null;
        if (!ValidateUtils.isNullObject(requestDTO.getEmployeeShiftDTO()) && !ValidateUtils.isLongZero(requestDTO.getEmployeeShiftDTO().getId())){
            employeeShift = employeeShiftRepository.findById(requestDTO.getEmployeeShiftDTO().getId())
                          .orElseThrow(()-> new ValidationException(messageResource.getEnglishMessage("employee.shift.not.found")));
        }

        List<ApprovalGroupBean> employeeApprovalGroupBeanList = employeeConfigRepository.getEmployeeApprovalGroup(empId);
        if(CollectionUtils.isEmpty(employeeApprovalGroupBeanList)){
            throw new ValidationException(messageResource.getEnglishMessage("approval.group.not.assigned"));
        }

        // 5. Build the parent entity FIRST (without approver list)
        EmployeeSwipeRequest employeeSwipeRequest = EmployeeSwipeRequest.builder()
                .employee(employee)
                .swipeDate(swipeDate)  // reuse the already converted date
                .dayType(EnumConverter.toEnum(requestDTO.getDayType(), DayType.class))
                .swipeDirection(EnumConverter.toEnum(requestDTO.getSwipeDirection(), SwipeDirection.class))
                .employeeShift(employeeShift)
                .reqInTime(TimeHelper.convertToLocalTime(requestDTO.getReqInTime()))
                .reqOutTime(TimeHelper.convertToLocalTime(requestDTO.getReqOutTime()))
                .swipeStatus(SwipeStatus.PENDING)
                .remark(requestDTO.getRemark())
                .createdBy(clinicId)
                .modifiedBy(clinicId)
                .build();

        // Now build the child entities with reference to parent
        List<SwipeRequestApproverDetails> approverDetailsList = new ArrayList<>();
        //using i for counter
        //for first sequence we will store pending and for other it will be null
        int i = 0;
        for (ApprovalGroupBean approvalGroupBean : employeeApprovalGroupBeanList) {

            UUID approverId = UUID.fromString(approvalGroupBean.getApproverEmpId());

            // Fetch the approver employee
            UUID approverEmployee = employeeRepository.getEmpId(String.valueOf(UUID.fromString(String.valueOf(approverId))));
            if (ValidateUtils.isNullObject(approverEmployee)) {
               throw new ValidationException(messageResource.getEnglishMessage("approver.employee.not.found"));
            }
            SwipeRequestApproverDetails requestApproverDetails = SwipeRequestApproverDetails.builder()
                    .approverEmployeeId(String.valueOf(approverEmployee))
                    .sequence(approvalGroupBean.getApprovalSequence())
                    .swipeStatus(i == 0 ? SwipeStatus.PENDING : null)
                    .employeeSwipeRequest(employeeSwipeRequest)  // SET PARENT REFERENCE
                    .build();
            i++;
            approverDetailsList.add(requestApproverDetails);
        }

        // Set the list on the parent
        employeeSwipeRequest.setApproverDetailsList(approverDetailsList);

        return employeeSwipeRequest;
    }


    private void convertForUpdate(EmployeeSwipeRequest entity, EmployeeSwipeRequestDTO dto, EmployeeShift employeeShift, String clinicId, String empId){
        entity.setDayType(EnumConverter.toEnum(dto.getDayType(), DayType.class));
        entity.setSwipeDirection(EnumConverter.toEnum(dto.getSwipeDirection(), SwipeDirection.class));
        entity.setEmployeeShift(employeeShift);
        entity.setReqInTime(TimeHelper.convertToLocalTime(dto.getReqInTime()));
        entity.setReqOutTime(TimeHelper.convertToLocalTime(dto.getReqOutTime()));
        entity.setSwipeStatus(EnumConverter.toEnum(dto.getSwipeStatus(), SwipeStatus.class));
        entity.setSwipeStatus(SwipeStatus.PENDING);
        entity.setRemark(dto.getRemark());
        entity.setModifiedBy(clinicId);
    }


    private EmployeeSwipeRequestDTO convertBeanToDto(List<EmployeeSwipeRequestBean> beans){
        if (CollectionUtils.isEmpty(beans)) {
            throw new ValidationException(messageResource.getEnglishMessage("bean.list.cannot.be.empty"));
        }

        EmployeeSwipeRequestBean swipeBean = beans.get(0);

        EmployeeShiftDTO employeeShiftDTO = null;
        if (!ValidateUtils.isLongZero(swipeBean.getEmpShiftId())){
            employeeShiftDTO = EmployeeShiftDTO.builder()
                    .id(swipeBean.getEmpShiftId())
                    .empId(swipeBean.getShiftEmpId())
                    .shiftId(swipeBean.getShiftId())
                    .shiftName(swipeBean.getShiftName())
                    .shiftCode(swipeBean.getShiftCode())
                    .shiftInTime(TimeHelper.convertToString(swipeBean.getShiftInTime()))
                    .shiftOutTime(TimeHelper.convertToString(swipeBean.getShiftOutTime()))
                    .firstHalfOutTime(TimeHelper.convertToString(swipeBean.getFirstHalfOutTime()))
                    .secondHalfInTime(TimeHelper.convertToString(swipeBean.getSecondHalfInTime()))
                    .date(DateHelper.formatDateObjectToUserDateView(swipeBean.getEmpShiftDate()))
                    .isDefault(swipeBean.getIsDefault())
                    .build();
        }
        List<EmployeeSwipeRequestBean> filteredApproverBeans = filterApproverList(beans);

        List<SwipeRequestApproverDetailsDTO> approverDetailsDTOList = filteredApproverBeans.stream()
                .filter(bean -> !ValidateUtils.isNullObject(bean.getSwipeRequestApproverId()))
                .map(approverBean -> SwipeRequestApproverDetailsDTO.builder()
                        .id(approverBean.getSwipeRequestApproverId())
                        .sequence(approverBean.getSequence())
                        .approverId(approverBean.getApproverId())
                        .approverName(approverBean.getApproverName())
                        .approverCode(approverBean.getApproverCode())
                        .swipeStatus(approverBean.getApproverSwipeStatus())
                        .actionDateTime(approverBean.getActionDateTime() == null ? null : DateHelper.formatDateTimeObjectToUserDateView(approverBean.getActionDateTime()))
                        .approverRemark(approverBean.getApproverRemark())
                        .build())
                        .collect(Collectors.toList());

        return EmployeeSwipeRequestDTO.builder()
                .id(swipeBean.getId())
                .empId(swipeBean.getRequestEmpId())
                .swipeDate(DateHelper.formatDateObjectToUserDateView(swipeBean.getSwipeDate()))
                .applicationDate(DateHelper.formatDateObjectToUserDateView(swipeBean.getApplicationDate()))
                .dayType(EnumConverter.toDisplayString(swipeBean.getDayType(), DayType.class))
                .swipeDirection(EnumConverter.toDisplayString(swipeBean.getSwipeDirection(), SwipeDirection.class))
                .employeeShiftDTO(employeeShiftDTO)
                .reqInTime(TimeHelper.convertToString(swipeBean.getReqInTime()))
                .reqOutTime(TimeHelper.convertToString(swipeBean.getReqOutTime()))
                .swipeStatus(EnumConverter.toDisplayString(swipeBean.getSwipeStatus(), SwipeStatus.class))
                .remark(swipeBean.getRemark())
                .createdDateTime(DateHelper.formatDateTimeObjectToUserDateView(swipeBean.getCreatedDateTime()))
                .modifiedDateTime(DateHelper.formatDateTimeObjectToUserDateView(swipeBean.getModifiedDateTime()))
                .createdBy(swipeBean.getCreatedBy())
                .modifiedBy(swipeBean.getModifiedBy())
                .approverDetailsDTOList(approverDetailsDTOList)
                .build();
    }

    private EmployeeShiftDetailDTO convertEmpShiftBeanToDto(EmployeeShiftDetailBean shiftDetailBean) {
        return EmployeeShiftDetailDTO.builder()
                .empShiftId(shiftDetailBean.getEmpShiftId())
                .shiftTemplateId(shiftDetailBean.getShiftTemplateId())
                .shiftName(shiftDetailBean.getShiftName())
                .shiftCode(shiftDetailBean.getShiftCode())
                .shiftInTime(TimeHelper.convertToString(shiftDetailBean.getShiftInTime()))
                .shiftOutTime(TimeHelper.convertToString(shiftDetailBean.getShiftOutTime()))
                .firstHalfOutTime(TimeHelper.convertToString(shiftDetailBean.getFirstHalfOutTime()))
                .secondHalfInTime(TimeHelper.convertToString(shiftDetailBean.getSecondHalfInTime()))
                .empShiftDate(DateHelper.formatDateObjectToUserDateView(shiftDetailBean.getEmpShiftDate()))
                .build();
    }


    private List<EmployeeSwipeRequestBean> filterApproverList(List<EmployeeSwipeRequestBean> beans) {

        List<EmployeeSwipeRequestBean> sorted = beans.stream()
                                           .filter(b -> b.getSwipeRequestApproverId() != null)
                                           .sorted(Comparator.comparing(EmployeeSwipeRequestBean::getSequence))
                                           .toList();

        List<EmployeeSwipeRequestBean> result = new ArrayList<>();

        for (EmployeeSwipeRequestBean requestBean : sorted) {
            String status = requestBean.getApproverSwipeStatus();

            // Add APPROVED and continue
            if ("APPROVED".equalsIgnoreCase(status)) {
                result.add(requestBean);
                continue;
            }

            // REJECTED add and stop
            if ("REJECTED".equalsIgnoreCase(status)) {
                result.add(requestBean);
                break;
            }

            // First pending approver add & stop
            if ("PENDING".equalsIgnoreCase(status)) {
                result.add(requestBean);
                break;
            }

            // Null stop without adding
            if (status == null) {
                break;
            }
        }
        return result;
    }
}
