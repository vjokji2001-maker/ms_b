package com.smartcare.hrms.common.entity.attendance.application.leave;


import com.smartcare.hrms.common.entity.BaseEntityUUID;
import com.smartcare.hrms.common.enumeration.ApplicationStatus;
import com.smartcare.hrms.common.enumeration.ApplicationType;
import com.smartcare.hrms.common.enumeration.DayType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lv_od_rqst")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveODRequest extends BaseEntityUUID {


        @Column(name = "emp_id", nullable = false)
        private String employeeId;

        @Column(name = "yr_id", nullable = false)
        private Long yearId;

        @Column(name = "app_type", nullable = false)
        @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
        private ApplicationType applicationType;

        @Column(name = "lv_nm_id", nullable = false)
        private Long leaveNameId;

        @Column(name = "app_dt", nullable = false)
        private LocalDate applicationDate;

        @Column(name = "frm_dt")
        private LocalDate fromDate;

        @Enumerated(EnumType.STRING)
        @Column(name = "frm_dt_typ", length = 20)
        private DayType fromDateType;

        @Column(name = "to_dt")
        private LocalDate toDate;

        @Enumerated(EnumType.STRING)
        @Column(name = "to_dt_typ", length = 20)
        private DayType toDateType;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 10)
        private ApplicationStatus status ;

        @Column(name = "reason", columnDefinition = "TEXT")
        private String reason;

        @Column(name = "apprvr_rmrk", columnDefinition = "TEXT")
        private String approverRemark;

        @Column(name="ttl_cnt")
        private Double totalCount;

        @Column(name="created_by")
        private String createdBy;

        @Column(name="modified_by")
        private String modifiedBy;

        @Column(name="modified_datetime",insertable = false, updatable = false)
        private LocalDateTime modifiedDateTime;

    @Column(name = "leave_rqst_id")
        private String leaveRequestId;

        /** One Leave Request = Many Approval Steps */
        @OneToMany(mappedBy = "leaveODRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<LeaveODRequestDtls> requestDetails;
    }
