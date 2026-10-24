package com.smartcare.hrms.hiring.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.smartcare.hrms.hiring.model.entity.enums.RequestStatus;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class HiringStatusResponse {

    private Long id;
    private String message;
    private RequestStatus status;
}
