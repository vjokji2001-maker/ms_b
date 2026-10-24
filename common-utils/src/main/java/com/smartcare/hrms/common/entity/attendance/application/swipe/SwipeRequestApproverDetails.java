package com.smartcare.hrms.common.entity.attendance.application.swipe;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.enumeration.SwipeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "swp_rqst_aprvr_dtl")
@Data
@Builder
public class SwipeRequestApproverDetails extends BaseEntityId {

    @ManyToOne(fetch = FetchType.LAZY)   // Many approver records belong to one swipe request
    @JoinColumn(name = "swp_rqst_id")
    private EmployeeSwipeRequest employeeSwipeRequest;

    @Column(name = "sequence")
    private Integer  sequence;

    @Column(name = "approver_id", updatable = false, nullable = false, columnDefinition = "uuid")
    private String approverEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SwipeStatus swipeStatus;

    @Column(name = "remark")
    private String remark;

    @Column(name = "action_date_time")
    private LocalDateTime actionDateTime;

}
