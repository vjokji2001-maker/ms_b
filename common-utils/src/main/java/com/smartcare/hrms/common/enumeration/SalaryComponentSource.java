package com.smartcare.hrms.common.enumeration;

public enum SalaryComponentSource {
    SYSTEM_CALCULATED,   // Auto calculated by system rules
    SYSTEM_CONFIGURED,   // Added by admin via UI
    EXCEL_IMPORTED       // Added via bulk upload / Excel
}
