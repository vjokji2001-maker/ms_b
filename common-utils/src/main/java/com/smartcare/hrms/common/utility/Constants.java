package com.smartcare.hrms.common.utility;

public class    Constants {
    public static final String SCHEMA_USERID	= "ChaluRam24";
    public static final String SCHEMA_PASSWORD 	= "BhaiG1ldjlFattuB3ta13@340435";
    public static final String SCHEMA_HOST 		= "jdbc:mysql://103.177.84.241/";
    public static final String ADMIN_HOST		= "jdbc:mysql://103.177.84.241/admin?useTimezone=true&serverTimezone=UTC";
    public static final String URL_HOST			= "jdbc:mysql:/103.177.84.241/schemaname?useTimezone=true&serverTimezone=UTC";
    public static final String SCHEMA_DRIVER 	= "com.mysql.cj.jdbc.Driver";
    public static final String INDIAN_ZONEID    = "Asia/Kolkata";
    public static final String HRMS_CLINIC_SUFFIX = "_hrms";
    // Smart care main (save user API)
    public static final String SMARTCARE_BASE_URL = "http://103.159.239.222:9090/smartcaremain";
//    public static final String SMARTCARE_BASE_URL = "http://localhost:8081/smartcaremain";
    public static final String SMARTCARE_SAVE_USER_ENDPOINT = SMARTCARE_BASE_URL + "/save/user/hrms";
    public static final String SALARY_ATTENDANCE_SAVE= "http://localhost:9099/hr/payroll/emp/payroll/summary/batch:Create";
}

