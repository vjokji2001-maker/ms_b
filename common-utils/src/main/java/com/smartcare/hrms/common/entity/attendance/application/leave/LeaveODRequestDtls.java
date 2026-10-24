package com.smartcare.hrms.common.entity.attendance.application.leave;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lv_od_rqst_dtls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveODRequestDtls extends BaseEntityId {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lv_rqst_id", nullable = false)
    private LeaveODRequest leaveODRequest;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "aprvr_id", length = 36)
    private String approverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private ApplicationStatus status ;


    @Column(name = "rmrk", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "action_date_time")
    private LocalDateTime actionDateTime;

}
