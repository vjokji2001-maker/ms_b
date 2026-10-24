package com.smartcare.hrms.migration.dto.master.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaveNameUploadDTO {

    private String name , code;
}
