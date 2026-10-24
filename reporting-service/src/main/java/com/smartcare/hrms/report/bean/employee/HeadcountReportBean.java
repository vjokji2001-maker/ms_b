    package com.smartcare.hrms.report.bean.employee;

    import java.util.Date;

    public interface HeadcountReportBean {

        String getDepartmentCode();

        String getDepartmentName();

        Long getCurrentHeadcount();

        Long getActiveEmployees();

        Long getProbationEmployees();

        Long getConfirmedEmployees();

        Long getPermanentEmployees();

        Long getContractEmployees();

        Long getMaleEmployees();

        Long getFemaleEmployees();

        Long getNewJoiners();

        Long getEmployeesOnNoticePeriod();

        Long getResignedEmployees();

        Long getTerminatedEmployees();

        Long getExitedEmployees();

        String getBranchName();

        String getWorkLocation();

        Date getLastUpdatedDate();

    }
