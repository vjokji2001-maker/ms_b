package com.smartcare.hrms.common.bean.employee;

public interface EmployeeConfigBean {

	String getEmployeeName();
	String getEmployeeCode();	
	String getCompanyName();
	String getBranchName();
	String getDepartmentName();
	String getDesignationName();
	String getWeekOffName();
	String getGrade();
	String getCategory();
	String getLocation();
	String getHolidayName();
	String getApprovalGroupName();
	String getYearCycleName();
	Long getEmployeeLeaveParentId();
	Long getFutureDays();
	Long getPastDays();
	Boolean getHalfDayAllowed();
	Double getMonthlyCount();
	Double getQuaterlyCount();
	Double getYearlyCount();
	String getLeaveName();
	String getLeaveTemplateName();
    Long getLeaveNameId();
}
