package com.smartcare.hrms.common.dto.master.attendance.approvalgroup;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ApprovalGroupDTO {
    private String id;
    private String name;
    private String code;
    private List<ApprovalGroupDtlsDTO> approvalGroupDtlsDTOList;

}
