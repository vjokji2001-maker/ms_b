package com.smartcare.hrms.migration.dto.master.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceMasterUploadResultDTO {

    private AttendanceMasterSheetResultDTO holidayResult;
    private AttendanceMasterSheetResultDTO holidayDatesResult;
    private AttendanceMasterSheetResultDTO shiftTemplateResult;
    private AttendanceMasterSheetResultDTO weekOffResult;
    private AttendanceMasterSheetResultDTO weekOffDetailsResult;
    private AttendanceMasterSheetResultDTO leaveNameResult;
    private AttendanceMasterSheetResultDTO leaveTemplateResult;
    private AttendanceMasterSheetResultDTO leaveTemplateDtlsResult;
    private AttendanceMasterSheetResultDTO approvalGroupResult;
    private AttendanceMasterSheetResultDTO approvalGroupDtlsResult;
    private AttendanceMasterSheetResultDTO monthCycleResult;
    private AttendanceMasterSheetResultDTO yearCycleResult;

}
