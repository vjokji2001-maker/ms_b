package com.smartcare.hrms.common.entity.master.attendance.approvalgroup;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.entity.employee.Employee;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="aprvl_grp_dtls")
public class ApprovalGroupDtls extends BaseEntityId {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprvl_grp_id")
    private ApprovalGroup approvalGroup;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name="sequence")
    private Integer sequence;
}
