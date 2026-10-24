package com.smartcare.hrms.attendance.validator;

import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveRequestRepository;
import com.smartcare.hrms.common.bean.attendance.EmployeeLeaveDataBean;
import com.smartcare.hrms.common.bean.application.leave.LeaveRequestBean;
import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.bean.employee.EmployeeMonthCycleBean;
import com.smartcare.hrms.common.bean.employee.EmployeeWeekOffBean;
import com.smartcare.hrms.common.entity.master.attendance.period.YearCycle;
import com.smartcare.hrms.common.enumeration.DayType;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.repository.employee.EmployeeLeaveParentRepository;
import com.smartcare.hrms.common.repository.employee.EmployeeRepository;
import com.smartcare.hrms.common.repository.master.attendance.period.YearCycleRepository;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LeaveRequestValidator {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeLeaveParentRepository employeeLeaveParentRepository;

    @Autowired
    private YearCycleRepository yearCycleRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private MessageResource messageResource;


    public void validateLeaveRequest(LeaveODRequestDTO requestDTO){

        // check for invalid request
         validateInvalidDayTypeRequest(requestDTO);
        // Check leave request dto
         validateRequestDataDTO(requestDTO);
         //get and check
        Optional<YearCycle> yearCycleOptional = yearCycleRepository.findById(requestDTO.getYearId());
        if(yearCycleOptional.isEmpty()){
            throw new ValidationException(messageResource.getEnglishMessage("year.not.found"));
        }

        EmployeeLeaveDataBean empYrLeaves = employeeLeaveParentRepository.getLeaveDetailsByYearAndEmpId(requestDTO.getEmployeeId(), requestDTO.getYearId(), requestDTO.getLeaveNameId());
        if(ValidateUtils.isNullObject(empYrLeaves)){
            throw new ValidationException(messageResource.getEnglishMessage("emp.leave.not.defined"));
        }

        // month cycle
        EmployeeMonthCycleBean employeeMonthCycle = employeeRepository.getEmployeeMonthCycle(requestDTO.getEmployeeId());
        if(ValidateUtils.isNullObject(employeeMonthCycle)){
            throw new ValidationException(messageResource.getEnglishMessage("emp.month.cycle.defined"));
        }
        /* Year cycle date */
        YearCycle yearCycle = yearCycleOptional.get();
        Map<String, LocalDate> yearStartEndDateMap = DateHelper.getYearStartEndDate(yearCycle.getFromMonthYear(), employeeMonthCycle.getStartDate());

        LocalDate yearStartDate = yearStartEndDateMap.get("yearStartDate");
        LocalDate yearEndDate = yearStartEndDateMap.get("yearEndDate");

        /*Validation as per year cycle*/
        validateLeaveDateBetweenYearCycle(requestDTO,yearStartDate,yearEndDate);

        validateTotalCountWithWeekOffSkip(requestDTO);

        /* check balance and allowed leaves */
        checkLeaveBalanceValidation(empYrLeaves,employeeMonthCycle,yearStartDate,yearEndDate,requestDTO);
    }

    private void validateRequestDataDTO(LeaveODRequestDTO requestDTO){
        // Leave validation
        if(ValidateUtils.isLongZero(requestDTO.getLeaveNameId())){
            throw new ValidationException(messageResource.getEnglishMessage("leave.name.not.found"));
        }
        // employee validation
        if(ValidateUtils.isStringEmpty(requestDTO.getEmployeeId()) ||
                ValidateUtils.isNullObject(employeeRepository.getEmpId(requestDTO.getEmployeeId()))){
            throw new ValidationException(messageResource.getEnglishMessage("employee.not.found"));
        }
        // year validation
        if(ValidateUtils.isLongZero(requestDTO.getYearId())){
            throw new ValidationException(messageResource.getEnglishMessage("year.not.found"));
        }
    }

    private void validateInvalidDayTypeRequest(LeaveODRequestDTO requestDTO){
        if(requestDTO.getFromDate().isAfter(requestDTO.getToDate())){
            throw new ValidationException(messageResource.getEnglishMessage("invalid.date.range"));
        }else if(requestDTO.getToDate().isAfter(requestDTO.getFromDate())){
            if(DayType.FIRST_HALF.toString().equals(requestDTO.getFromDateDuration())){
                throw new ValidationException(messageResource.getEnglishMessage("invalid.day.type"));
            } else if (DayType.SECOND_HALF.toString().equals(requestDTO.getToDateDuration())) {
                throw new ValidationException(messageResource.getEnglishMessage("invalid.day.type"));
            }
        }
    }

    private void validateLeaveDateBetweenYearCycle(LeaveODRequestDTO requestDTO, LocalDate yearStartDate, LocalDate yearEndDate){
        /*Validation as per year cycle*/
        if((requestDTO.getFromDate().isBefore(yearStartDate) || requestDTO.getFromDate().isAfter(yearEndDate))){
            throw new ValidationException(messageResource.getEnglishMessage("from.date.out.of.year.range"));
        }else if((requestDTO.getToDate().isBefore(yearStartDate) || requestDTO.getToDate().isAfter(yearEndDate))){
            throw new ValidationException(messageResource.getEnglishMessage("to.date.out.of.year.range"));
        }
    }



    private void checkLeaveBalanceValidation(EmployeeLeaveDataBean empYrLeaves, EmployeeMonthCycleBean employeeMonthCycle, LocalDate yearStartDate, LocalDate yearEnddDate, LeaveODRequestDTO requestDTO){
            LocalDate leaveFromDate = requestDTO.getFromDate();
            LocalDate leaveToDate   = requestDTO.getToDate();

            Integer masterMonthStartDate = employeeMonthCycle.getStartDate();
            Integer masterMonthEndDate = employeeMonthCycle.getEndDate();

            //check leave allowed to apply criteria (past and future days & halfday)
            validateLeaveAllowedCriteria(requestDTO,empYrLeaves,leaveFromDate,leaveToDate);

            //fetching approved and pending employee leaves
            List<LeaveRequestBean> empExistingApprovedPendingLeavs = leaveRequestRepository.getEmployeeLeaveRequestDataByEmpIdAndYearId(requestDTO.getEmployeeId(), requestDTO.getYearId());

            //yearly leaves
          double totalLeavesTaken = empExistingApprovedPendingLeavs.stream().mapToDouble(value -> value.getTotalLeaveCount()).sum();
            if(requestDTO.getTotalCount()>(empYrLeaves.getYearlyCount() - totalLeavesTaken )){
                throw new ValidationException(messageResource.getEnglishMessage("insufficient.leave.balance"));
            }

            // Quarterly
            // get quarter as per fromdate i.e might be the past quarter
            double quarterlyAllowedLeaveCount = empYrLeaves.getQuarterlyCount();

            Map<String, LocalDate> quarterStartEndDate = DateHelper.getQuarterStartEndDate(masterMonthStartDate, masterMonthEndDate, yearStartDate, leaveFromDate);
            LocalDate quarterStartDate = quarterStartEndDate.get("qStartDate");
            LocalDate quarterEndDate = quarterStartEndDate.get("qEndDate");
            if(leaveToDate.isAfter(quarterEndDate)){
                Map<String, LocalDate> nextQuarterStartEndDate = DateHelper.getQuarterStartEndDate(masterMonthStartDate, masterMonthEndDate, yearStartDate, leaveToDate);
                quarterEndDate = nextQuarterStartEndDate.get("qEndDate");
                if(quarterlyAllowedLeaveCount> empYrLeaves.getYearlyCount()){
                    quarterlyAllowedLeaveCount =empYrLeaves.getYearlyCount();
                }else{
                    quarterlyAllowedLeaveCount*=2;
                }
            }
            // Monthly
            // Monthly check
            Map<String, LocalDate> monthStartEndDate = DateHelper.getMonthStartEndDate(masterMonthStartDate, masterMonthEndDate, leaveFromDate);
            LocalDate monthStartDate = monthStartEndDate.get("monthStartDate");
            LocalDate monthEndDate = monthStartEndDate.get("monthEndDate");
            double monthlyAllowedLeaves = empYrLeaves.getMonthlyCount();
            if(monthEndDate.isBefore(leaveToDate)){
                Map<String, LocalDate> nextMonthEndDate = DateHelper.getMonthStartEndDate(masterMonthStartDate, masterMonthEndDate, leaveToDate);
                monthEndDate = nextMonthEndDate.get("monthEndDate");
                if(monthlyAllowedLeaves>quarterlyAllowedLeaveCount){
                    monthlyAllowedLeaves=quarterlyAllowedLeaveCount;
                }else{
                    monthlyAllowedLeaves*=2;
                }
            }


            if(!CollectionUtils.isEmpty(empExistingApprovedPendingLeavs)){

                //compre requested dates with existing fromdate and todate
                validateRequestedDatesWithExitingDate(empExistingApprovedPendingLeavs,requestDTO);

                //check requested date exist inbetween already pending or approved
                validateDatesIfExistInBetween(empExistingApprovedPendingLeavs,leaveFromDate,leaveToDate);

                //check quarterly Leaves
                LocalDate finalQuarterEndDate = quarterEndDate;
                List<LeaveRequestBean> employeeLeavesTaken = empExistingApprovedPendingLeavs.stream().filter(predicate -> !predicate.getFromDate().isBefore(quarterStartDate) && !predicate.getToDate().isAfter(finalQuarterEndDate)).collect(Collectors.toList());
                double totalLeavesTakenCount = employeeLeavesTaken.stream().mapToDouble(value -> value.getTotalLeaveCount()).sum();
                double remainingLeaves =  quarterlyAllowedLeaveCount-totalLeavesTakenCount;
                if(requestDTO.getTotalCount()>remainingLeaves){
                    throw new ValidationException(messageResource.getEnglishMessage("quarterly.insufficient.balance"));
                }
                //check monthly leaves
                //employee approved or pending monthly leaves
                List<LeaveRequestBean> leaveRequestBeans = employeeLeavesTaken.stream().filter(pre -> !pre.getFromDate().isBefore(leaveFromDate) && !pre.getToDate().isAfter(leaveToDate)).collect(Collectors.toList());
                double monthlytakenLeaveCount = leaveRequestBeans.stream().mapToDouble(value -> value.getTotalLeaveCount()).sum();
                if(monthlytakenLeaveCount>monthlyAllowedLeaves){
                    throw new ValidationException(messageResource.getEnglishMessage("month.insufficient.balance"));
                }

            }else {
                //fresh leave request for the year
                if(requestDTO.getTotalCount()> empYrLeaves.getYearlyCount()){
                    throw new ValidationException(messageResource.getEnglishMessage("yearly.insufficient.balance"));
                }else if(requestDTO.getTotalCount()> quarterlyAllowedLeaveCount){
                    throw new ValidationException(messageResource.getEnglishMessage("quarterly.insufficient.balance"));
                }else if(requestDTO.getTotalCount()> monthlyAllowedLeaves){
                    throw new ValidationException(messageResource.getEnglishMessage("quarterly.insufficient.balance"));
                }
            }
    }

    private void validateDatesIfExistInBetween(List<LeaveRequestBean> empExistingApprovedPendingLeavs,LocalDate leaveFromDate,LocalDate leaveToDate){
        /*
                 check if requested from date is already exist in between some other
                 peding or approved leave
                 */
        boolean fromDateAlreadyExstFlag = empExistingApprovedPendingLeavs.stream().anyMatch(p -> leaveFromDate.isAfter(p.getFromDate()) && leaveFromDate.isBefore(p.getToDate()));
        if(fromDateAlreadyExstFlag){
            throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
        }

        boolean toDateAlreadyExstFlag = empExistingApprovedPendingLeavs.stream().anyMatch(p -> leaveToDate.isAfter(p.getFromDate()) && leaveToDate.isBefore(p.getToDate()));
        if(toDateAlreadyExstFlag){
            throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
        }
    }

    private void validateLeaveAllowedCriteria(LeaveODRequestDTO requestDTO, EmployeeLeaveDataBean empYrLeaves, LocalDate leaveFromDate, LocalDate leaveToDate){
        if(requestDTO.getApplicationDate().isAfter(leaveFromDate)){
            // check past days allowed validation
            Long appliedPastDaysSpan = DateHelper.calculateTotalSpan(leaveFromDate,requestDTO.getApplicationDate());
            if(appliedPastDaysSpan > empYrLeaves.getPastDaysAllowed()){
                throw new ValidationException(messageResource.getEnglishMessageWithParam("days.allowed.date.range.exceeded",new String[]{"Past"}));
            }
        }else{
            // check futuredays validation
            Long appliedFutureDaysSpan = DateHelper.calculateTotalSpan(requestDTO.getApplicationDate(), leaveToDate);
            if(appliedFutureDaysSpan>empYrLeaves.getFutureDaysAllowed()){
                throw new ValidationException(messageResource.getEnglishMessageWithParam("days.allowed.date.range.exceeded",new String[]{"Future"}));
            }
        }
        /* Days allowed Validation END */
        if(requestDTO.getTotalCount()%1==0.5 && !empYrLeaves.getHalfDayAllowed()){
            throw new ValidationException(messageResource.getEnglishMessage("halfday.leave.not.allowed"));
        }
        // create method for year balance validation
        // create method for quarter balance validation
        // create method for monthly balance validation

    }


    private void validateRequestedDatesWithExitingDate(List<LeaveRequestBean> empExistingApprovedPendingLeavs , LeaveODRequestDTO requestDTO){
        LocalDate leaveFromDate = requestDTO.getFromDate();
        LocalDate leaveToDate   = requestDTO.getToDate();
        if(leaveFromDate.isBefore(leaveToDate)){
            {
                //fromdate daytype duration
                List<LeaveRequestBean> leaveRequestBeans = empExistingApprovedPendingLeavs.stream().filter(p -> p.getFromDate().equals(leaveFromDate)).collect(Collectors.toList());
                if(leaveRequestBeans.size()==2){
                    throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
                }else{
                    LeaveRequestBean leaveRequestBean = leaveRequestBeans.get(0);
                    if(leaveRequestBean.getFromDateType().equals(DayType.FULL_DAY) || leaveRequestBean.getFromDateType().equals(DayType.fromValue(requestDTO.getFromDateDuration()))){
                        throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
                    }
                }
                leaveRequestBeans = empExistingApprovedPendingLeavs.stream().filter(p -> p.getFromDate().equals(leaveToDate)).collect(Collectors.toList());
                if(leaveRequestBeans.size()==2){
                    throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
                }else{
                    LeaveRequestBean leaveRequestBean = leaveRequestBeans.get(0);
                    if(leaveRequestBean.getToDateType().equals(DayType.FULL_DAY) || leaveRequestBean.getToDateType().equals(DayType.fromValue(requestDTO.getFromDateDuration()))){
                        throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
                    }
                }
            }
            // todate
            {
                //fromdate daytype duration
                List<LeaveRequestBean> leaveRequestBeans = empExistingApprovedPendingLeavs.stream().filter(p -> p.getToDate().equals(leaveFromDate)).collect(Collectors.toList());
                if(leaveRequestBeans.size()==2){
                    throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
                }else{
                    LeaveRequestBean leaveRequestBean = leaveRequestBeans.get(0);
                    if(leaveRequestBean.getFromDateType().equals(DayType.FULL_DAY) || leaveRequestBean.getFromDateType().equals(DayType.fromValue(requestDTO.getToDateDuration()))){
                        throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
                    }
                }
                leaveRequestBeans = empExistingApprovedPendingLeavs.stream().filter(p -> p.getToDate().equals(leaveToDate)).collect(Collectors.toList());
                if(leaveRequestBeans.size()==2){
                    throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
                }else{
                    LeaveRequestBean leaveRequestBean = leaveRequestBeans.get(0);
                    if(leaveRequestBean.getToDateType().equals(DayType.FULL_DAY) || leaveRequestBean.getToDateType().equals(DayType.fromValue(requestDTO.getToDateDuration()))){
                        throw new ValidationException(messageResource.getEnglishMessage("todate.already.exists.for.requested.period"));
                    }
                }
            }

        }else{
            //single day
            List<LeaveRequestBean> leaveRequestBeans = empExistingApprovedPendingLeavs.stream().filter(p -> p.getFromDate().equals(leaveFromDate)).collect(Collectors.toList());
            boolean isLeaveAlreadyExist = empExistingApprovedPendingLeavs.stream()
                    .anyMatch(p -> p.getFromDate().equals(leaveFromDate) &&
                            (p.getFromDateType().equals(DayType.FULL_DAY)|| p.getFromDateType().equals(DayType.fromValue(requestDTO.getFromDateDuration()))));
            if(isLeaveAlreadyExist){
                throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
            }
        }


    }

    private void subValidatorForExistingLeave(List<LeaveRequestBean> existingLeaveRequestBeans,DayType durationType){
        if(!CollectionUtils.isEmpty(existingLeaveRequestBeans)){
            if(existingLeaveRequestBeans.size()==2){
                throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
            }else{
                LeaveRequestBean leaveRequestBean = existingLeaveRequestBeans.get(0);
                if(leaveRequestBean.getFromDateType().equals(DayType.FULL_DAY) || leaveRequestBean.getFromDateType().equals(durationType)){
                    throw new ValidationException(messageResource.getEnglishMessage("fromdate.already.exists.for.requested.period"));
                }
            }
        }
    }

    private void validateTotalCountWithWeekOffSkip(LeaveODRequestDTO requestDTO) {

        List<EmployeeWeekOffBean> weekOffList =
                employeeRepository.getEmployeeWeekDetail(requestDTO.getEmployeeId());

        LocalDate fromDate = requestDTO.getFromDate();
        LocalDate toDate   = requestDTO.getToDate();

        double calculatedDays = 0.0;

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {

            // Skip week off days
            if (isWeekOffDate(date, weekOffList)) {
                continue;
            }

            // HALF DAY LEAVE
            if (isHalfDayLeave(requestDTO)) {
                calculatedDays += 0.5;
            }
            // FULL DAY LEAVE
            else {
                calculatedDays += 1.0;
            }
        }

        if (Double.compare(calculatedDays, requestDTO.getTotalCount()) != 0) {
            throw new ValidationException(
                    messageResource.getEnglishMessage("invalid.leave.total.count"));
        }
    }

    private boolean isHalfDayLeave(LeaveODRequestDTO requestDTO) {

        return "FIRST_HALF".equalsIgnoreCase(requestDTO.getFromDateDuration())
                || "SECOND_HALF".equalsIgnoreCase(requestDTO.getFromDateDuration());
    }
    private boolean isWeekOffDate(LocalDate date, List<EmployeeWeekOffBean> weekOffList) {

        LocalDate month = date.withDayOfMonth(1);

        for (EmployeeWeekOffBean weekOff : weekOffList) {

            DayOfWeek weekDay = DayOfWeek.valueOf(weekOff.getDay());
            int weekIndex = getWeekIndex(weekOff.getWeekNo());

            Optional<LocalDate> weekOffDate =
                    getNthWeekdayOfMonth(month, weekDay, weekIndex);

            if (weekOffDate.isPresent() && weekOffDate.get().isEqual(date)) {
                return true;
            }
        }
        return false;
    }

    private int getWeekIndex(String weekNo) {
        return Integer.parseInt(weekNo.replace("WEEK_", ""));
    }
    private Optional<LocalDate> getNthWeekdayOfMonth(
            LocalDate month,
            DayOfWeek dayOfWeek,
            int nth) {

        LocalDate firstDayOfMonth = month.withDayOfMonth(1);

        int count = 0;

        for (int i = 0; i < month.lengthOfMonth(); i++) {
            LocalDate date = firstDayOfMonth.plusDays(i);

            if (date.getDayOfWeek() == dayOfWeek) {
                count++;
                if (count == nth) {
                    return Optional.of(date);
                }
            }
        }
        return Optional.empty();
    }
}
