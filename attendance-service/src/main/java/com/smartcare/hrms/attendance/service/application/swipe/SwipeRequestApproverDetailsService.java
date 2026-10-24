package com.smartcare.hrms.attendance.service.application.swipe;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dao.application.swipe.SwipeRequestApproverDetailsDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeSwipeRequestDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.SwipeRequestApproverDetailsDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.SwipeRequestApproverDetailsDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.SwipeRequestApproverSearchDTO;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDTO;
import com.smartcare.hrms.common.bean.attendance.SwipeRequestApproverDetailsBean;
import com.smartcare.hrms.common.entity.attendance.application.swipe.EmployeeDailyPunchLog;
import com.smartcare.hrms.common.entity.attendance.application.swipe.EmployeeSwipeRequest;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.enumeration.PunchType;
import com.smartcare.hrms.common.enumeration.SwipeDirection;
import com.smartcare.hrms.common.enumeration.SwipeStatus;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.repository.attendance.application.swipe.EmployeeDailyPunchLogRepository;
import com.smartcare.hrms.common.repository.attendance.application.swipe.EmployeeSwipeRequestRepository;
import com.smartcare.hrms.common.repository.attendance.application.swipe.SwipeRequestApproverDetailsRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeConfigRepository;
import com.smartcare.hrms.common.repository.master.attendance.approvalgroup.ApprovalGroupDtlsRepository;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.common.utility.TimeHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SwipeRequestApproverDetailsService {

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private SwipeRequestApproverDetailsDashboardDAO swipeRequestApproverDetailsDashboardDAO;

    @Autowired
    private SwipeRequestApproverDetailsRepository swipeRequestApproverDetailsRepository;

    @Autowired
    private EmployeeSwipeRequestRepository employeeSwipeRequestRepository;

    @Autowired
    private EmployeeDailyPunchLogRepository employeeDailyPunchLogRepository;

    @Autowired
    private EmployeeConfigRepository employeeConfigRepository;

    @Autowired
    private ApprovalGroupDtlsRepository approvalGroupDtlsRepository;


    /**
     *
     * @param searchDTO
     * @return Paginated response details
     */
    public PaginatedDataResponse<SwipeRequestApproverDetailsDashboardDTO> getPaginatedSwipeRequestList(SwipeRequestApproverSearchDTO searchDTO) {
        if (ValidateUtils.isZero(searchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        if (ValidateUtils.isStringEmpty(searchDTO.getApproverEmpId())){
            throw new ValidationException(messageResource.getEnglishMessage("swipe.approver.required"));
        }
        return swipeRequestApproverDetailsDashboardDAO.getData(searchDTO);
    }

    /**
     * Updates swipe request status and logs approver action by user.
     *
     * @param approverDetailsDTO approver details
     * @param clinicId           user identifier
     */
    @Transactional
    public void updateSingleSwipeRequestStatus(SwipeRequestApproverDetailsDTO approverDetailsDTO, String clinicId) {

        // 1. Validate approver ID
        if (ValidateUtils.isLongZero(approverDetailsDTO.getId())){
            throw new ValidationException(messageResource.getEnglishMessage("swipe.approver.required"));
        }

        // 2 validate swipe request
        EmployeeSwipeRequest employeeSwipeRequest = employeeSwipeRequestRepository.findById(approverDetailsDTO.getEmployeeSwipeRequestDTO().getId())
                .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.swipe.request.not.found")));

        // 3. Validate approver primary ID exists in the approver list
        boolean approverExists = employeeSwipeRequest.getApproverDetailsList()
                .stream()
                .anyMatch(approver -> approver.getId().equals(approverDetailsDTO.getId()));

        if (!approverExists) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.approver.not.found"));
        }

        // using this flag to update PENDING status for next approver if current one is approving
        AtomicBoolean statusChangedFlag = new AtomicBoolean(false);

        // setting final status 1=IN-PROGRESS, 2=APPROVED, 3=REJECTED
        AtomicInteger finalStatusFlag = new AtomicInteger(0);

        // iterating list and check if approverID matched will set status and remark
        employeeSwipeRequest.getApproverDetailsList().forEach(approverObject->{
            if(statusChangedFlag.get()){
                // setting next approver as pending if exist
                approverObject.setSwipeStatus(SwipeStatus.PENDING);
                finalStatusFlag.set(1);
                statusChangedFlag.set(false);
            }
            if(String.valueOf(approverObject.getApproverEmployeeId()).equals(approverDetailsDTO.getApproverId())){
                SwipeStatus approverStatus = EnumConverter.toEnum(approverDetailsDTO.getSwipeStatus(), SwipeStatus.class);
                approverObject.setSwipeStatus(approverStatus);
                approverObject.setActionDateTime(LocalDateTime.now());
                approverObject.setRemark(approverDetailsDTO.getApproverRemark());
                if(SwipeStatus.APPROVED.equals(approverStatus)){
                    statusChangedFlag.set(true);
                    finalStatusFlag.set(2);
                }else if(SwipeStatus.REJECTED.equals(approverStatus)){
                    finalStatusFlag.set(3);
                }
            }
        });
        SwipeStatus requestStatus = null;
        if(finalStatusFlag.get() == SwipeStatus.IN_PROGRESS.ordinal()){
            requestStatus=SwipeStatus.IN_PROGRESS;
        }else if(finalStatusFlag.get() == SwipeStatus.APPROVED.ordinal()){
            requestStatus=SwipeStatus.APPROVED;
        }else if(finalStatusFlag.get() == SwipeStatus.REJECTED.ordinal()){
            requestStatus=SwipeStatus.REJECTED;
        }
        employeeSwipeRequest.setSwipeStatus(requestStatus);
        employeeSwipeRequest.setModifiedDateTime(LocalDateTime.now());
        employeeSwipeRequest.setModifiedBy(clinicId);

        employeeSwipeRequestRepository.save(employeeSwipeRequest);

        // 8 Insert punch logs only when fully approved
        if (SwipeStatus.APPROVED.equals(requestStatus)) {
            insertDailyPunchLog(employeeSwipeRequest);
        }
    }

    /**
     * Retrieves a swipe request approver detail by its ID.
     *
     * @param id the approver detail ID
     * @return the corresponding SwipeRequestApproverDetailsDTO
     * @throws ValidationException if the ID is invalid or no record is found
     */
    public SwipeRequestApproverDetailsDTO getById(Long id) {
        if (ValidateUtils.isLongZero(id)) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.request.approver.not.found"));
        }
        List<SwipeRequestApproverDetailsBean> approverDetailsBeans = swipeRequestApproverDetailsRepository.getByApproverDtlId(id);
        if (CollectionUtils.isEmpty(approverDetailsBeans)) {
            throw new ValidationException(messageResource.getEnglishMessage("swipe.request.approver.not.found"));
        }
        return convertBeanToDto(approverDetailsBeans, id);

    }


    /**
     * Updates multiple swipe request statuses in bulk and logs approver actions.
     *
     * @param approverDetailsDTOList list of approver details
     * @param clinicId               user identifier
     */
    @Transactional
    public void updateBulkSwipeRequestStatus(List<SwipeRequestApproverDetailsDTO> approverDetailsDTOList, String clinicId){

        List<EmployeeSwipeRequest> requestsToSave = new ArrayList<>();
        List<EmployeeSwipeRequest> fullyApprovedRequests = new ArrayList<>();

        for (SwipeRequestApproverDetailsDTO approverDetailsDTO : approverDetailsDTOList) {
            // 1. Validate approver ID
            if (ValidateUtils.isLongZero(approverDetailsDTO.getId())) {
                throw new ValidationException(messageResource.getEnglishMessage("swipe.approver.required"));
            }

            // 2. Validate swipe request
            EmployeeSwipeRequest employeeSwipeRequest = employeeSwipeRequestRepository.findById(approverDetailsDTO.getEmployeeSwipeRequestDTO().getId())
                    .orElseThrow(() -> new ValidationException(messageResource.getEnglishMessage("employee.swipe.request.not.found")));


            // 3. Validate approver exists in the approver list
            boolean approverExists = employeeSwipeRequest.getApproverDetailsList()
                    .stream()
                    .anyMatch(approver -> approver.getId().equals(approverDetailsDTO.getId()));

            if (!approverExists) {
                throw new ValidationException(messageResource.getEnglishMessage("swipe.approver.not.found"));
            }

            // Process the approval logic
            AtomicBoolean statusChangedFlag = new AtomicBoolean(false);
            AtomicInteger finalStatusFlag = new AtomicInteger(0);

            employeeSwipeRequest.getApproverDetailsList().forEach(approverObject -> {
                if (statusChangedFlag.get()) {
                    // Setting next approver as pending if exists
                    approverObject.setSwipeStatus(SwipeStatus.PENDING);
                    finalStatusFlag.set(1);
                    statusChangedFlag.set(false);
                }
                if (String.valueOf(approverObject.getApproverEmployeeId()).equals(approverDetailsDTO.getApproverId())) {
                    SwipeStatus approverStatus = EnumConverter.toEnum(approverDetailsDTO.getSwipeStatus(), SwipeStatus.class);
                    approverObject.setSwipeStatus(approverStatus);
                    approverObject.setActionDateTime(LocalDateTime.now());
                    approverObject.setRemark(approverDetailsDTO.getApproverRemark());

                    if (SwipeStatus.APPROVED.equals(approverStatus)) {
                        statusChangedFlag.set(true);
                        finalStatusFlag.set(2);
                    } else if (SwipeStatus.REJECTED.equals(approverStatus)) {
                        finalStatusFlag.set(3);
                    }
                }
            });

            // Set final request status
            SwipeStatus requestStatus = null;
            if (finalStatusFlag.get() == SwipeStatus.IN_PROGRESS.ordinal()) {
                requestStatus = SwipeStatus.IN_PROGRESS;
            } else if (finalStatusFlag.get() == SwipeStatus.APPROVED.ordinal()) {
                requestStatus = SwipeStatus.APPROVED;
            } else if (finalStatusFlag.get() == SwipeStatus.REJECTED.ordinal()) {
                requestStatus = SwipeStatus.REJECTED;
            }

            employeeSwipeRequest.setSwipeStatus(requestStatus);
            employeeSwipeRequest.setModifiedDateTime(LocalDateTime.now());
            employeeSwipeRequest.setModifiedBy(clinicId);

            requestsToSave.add(employeeSwipeRequest);

            // Track fully approved requests for punch log insertion
            if (SwipeStatus.APPROVED.equals(requestStatus)) {
                fullyApprovedRequests.add(employeeSwipeRequest);
            }
        }

        // Bulk save all valid requests
        if (!requestsToSave.isEmpty()) {
            employeeSwipeRequestRepository.saveAll(requestsToSave);
        }

        // Insert punch logs for all fully approved requests
        for (EmployeeSwipeRequest approvedRequest : fullyApprovedRequests) {
            insertDailyPunchLog(approvedRequest);
        }
    }



    private SwipeRequestApproverDetailsDTO convertBeanToDto(List<SwipeRequestApproverDetailsBean> beans, Long searchedId) {

        // Identify the searched approverDtlId
        SwipeRequestApproverDetailsBean particular = beans.stream()
                        .filter(b -> b.getApproverDtlId().equals(searchedId))
                        .findFirst()
                        .orElseThrow(null);

        SwipeStatus swipeStatus = EnumConverter.toEnum(particular.getSwipeStatus(), SwipeStatus.class);

        // Build EmployeeShiftDTO
        EmployeeShiftDTO employeeShiftDTO = null;
        if (!ValidateUtils.isNullObject(particular) && !ValidateUtils.isLongZero(particular.getEmpShiftPkId())) {
            employeeShiftDTO = EmployeeShiftDTO.builder()
                    .id(particular.getEmpShiftPkId())
                    .empId(particular.getEmpShiftEmpId())
                    .shiftId(particular.getShiftId())
                    .shiftName(particular.getShiftName())
                    .shiftCode(particular.getShiftCode())
                    .shiftInTime(TimeHelper.convertToString(particular.getShiftInTime()))
                    .shiftOutTime(TimeHelper.convertToString(particular.getShiftOutTime()))
                    .firstHalfOutTime(TimeHelper.convertToString(particular.getFirstHalfOutTime()))
                    .secondHalfInTime(TimeHelper.convertToString(particular.getSecondHalfInTime()))
                    .date(DateHelper.formatDateObjectToUserDateView(particular.getEmpShiftDate()))
                    .isDefault(particular.getIsDefault())
                    .build();
        }

        // Build EmployeeSwipeRequestDTO
        EmployeeSwipeRequestDTO employeeSwipeRequestDTO = null;
        if (!ValidateUtils.isNullObject(particular.getEmpSwpRqstId())) {
            employeeSwipeRequestDTO = EmployeeSwipeRequestDTO.builder()
                    .id(particular.getEmpSwpRqstId())
                    .empId(particular.getEmpId())
                    .empName(particular.getName())
                    .empCode(particular.getCode())
                    .swipeDate(DateHelper.formatDateObjectToUserDateView(particular.getSwpDate()))
                    .applicationDate(DateHelper.formatDateObjectToUserDateView(particular.getApplicationDate()))
                    .dayType(EnumConverter.toDisplayString(particular.getDayType(), DayType.class))
                    .swipeDirection(EnumConverter.toDisplayString(particular.getSwipeDirection(), SwipeDirection.class))
                    .employeeShiftDTO(employeeShiftDTO)
                    .reqInTime(TimeHelper.convertToString(particular.getReqInTime()))
                    .reqOutTime(TimeHelper.convertToString(particular.getReqOutTime()))
                    .swipeStatus(EnumConverter.toDisplayString(particular.getEmpSwpStatus(), SwipeStatus.class))
                    .remark(particular.getEmpSwpRemark())
                    .createdDateTime(DateHelper.formatDateTimeObjectToUserDateView(particular.getCreatedDateTime()))
                    .modifiedDateTime(DateHelper.formatDateTimeObjectToUserDateView(particular.getModifiedDateTime()))
                    .createdBy(particular.getCreatedBy())
                    .modifiedBy(particular.getModifiedBy())
                    .build();

            // filter Approver list based on Pending, Approved, Rejected status
            List<SwipeRequestApproverDetailsBean> filteredBeans = filterApproverList(beans);

            // Build Approver List
            List<SwipeRequestApproverDetailsDTO> approverList = filteredBeans.stream()
                        .map(bean -> SwipeRequestApproverDetailsDTO.builder()
                                .id(bean.getApproverDtlId())
                                .sequence(bean.getSequence())
                                .approverId(bean.getApproverId())
                                .approverName(bean.getApproverName())
                                .approverCode(bean.getApproverCode())
                                .swipeStatus(EnumConverter.toDisplayString(bean.getSwipeStatus(), SwipeStatus.class))
                                .approverRemark(bean.getApproverRemark())
                                .actionDateTime(bean.getActionDateTime()  == null ? null : DateHelper.formatDateTimeObjectToUserDateView(bean.getActionDateTime()))
                                .build())
                                .toList();
            employeeSwipeRequestDTO.setApproverDetailsDTOList(approverList);
        }

        // Final particular approver DTO
        return SwipeRequestApproverDetailsDTO.builder()
                .id(particular.getApproverDtlId())
                .sequence(particular.getSequence())
                .approverId(particular.getApproverId())
                .swipeStatus(EnumConverter.toDisplayString(particular.getSwipeStatus(), SwipeStatus.class))
                .approverName(particular.getApproverName())
                .approverCode(particular.getApproverCode())
                .approverRemark(particular.getApproverRemark())
                .actionDateTime(particular.getActionDateTime() == null ? null : DateHelper.formatDateTimeObjectToUserDateView(particular.getActionDateTime()))
                .employeeSwipeRequestDTO(employeeSwipeRequestDTO)
                .build();

    }


    private void insertDailyPunchLog(EmployeeSwipeRequest swipeRequest) {
        SwipeDirection direction = swipeRequest.getSwipeDirection();
        LocalDate punchDate = swipeRequest.getSwipeDate();
        String empCode = swipeRequest.getEmployee().getEmpCode();

        if (direction == SwipeDirection.BOTH) {
            EmployeeDailyPunchLog inLog = EmployeeDailyPunchLog.builder()
                    .empCode(empCode)
                    .punchDate(punchDate)
                    .punchDateTime(punchDate.atTime(swipeRequest.getReqInTime()))
                    .punchType(PunchType.MANUAL)
                    .swipeDirection(SwipeDirection.IN)
                    .build();

            EmployeeDailyPunchLog outLog = EmployeeDailyPunchLog.builder()
                    .empCode(empCode)
                    .punchDate(punchDate)
                    .punchDateTime(punchDate.atTime(swipeRequest.getReqOutTime()))
                    .punchType(PunchType.MANUAL)
                    .swipeDirection(SwipeDirection.OUT)
                    .build();

            employeeDailyPunchLogRepository.saveAll(List.of(inLog, outLog));

        } else {
            LocalTime punchTime = (direction == SwipeDirection.IN) ? swipeRequest.getReqInTime() : swipeRequest.getReqOutTime();

            EmployeeDailyPunchLog punchLog = EmployeeDailyPunchLog.builder()
                    .empCode(empCode)
                    .punchDate(punchDate)
                    .punchDateTime(punchDate.atTime(punchTime))
                    .punchType(PunchType.MANUAL)
                    .swipeDirection(direction)
                    .build();

            employeeDailyPunchLogRepository.save(punchLog);
        }
    }

    private List<SwipeRequestApproverDetailsBean> filterApproverList(List<SwipeRequestApproverDetailsBean> beans) {

        List<SwipeRequestApproverDetailsBean> sorted = beans.stream()
                                             .sorted(Comparator.comparing(SwipeRequestApproverDetailsBean::getSequence))
                                             .toList();

        List<SwipeRequestApproverDetailsBean> result = new ArrayList<>();

        for (SwipeRequestApproverDetailsBean b : sorted) {
            String status = b.getSwipeStatus();

            // Add APPROVED and continue
            if ("APPROVED".equalsIgnoreCase(status)) {
                result.add(b);
                continue;
            }

            // REJECTED add and stop
            if ("REJECTED".equalsIgnoreCase(status)) {
                result.add(b);
                break;
            }

            // First pending approver add & stop
            if ("PENDING".equalsIgnoreCase(status)) {
                result.add(b);
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
