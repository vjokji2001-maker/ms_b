package com.smartcare.hrms.common.entity.master.attendance.approvalgroup;

import com.smartcare.hrms.common.entity.BaseEntityUUID;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="aprvl_grp")
public class ApprovalGroup extends BaseEntityUUID {

    @Column(name="name")
    private String name;

    @Column(name="code")
    private String code;

    @OneToMany(mappedBy = "approvalGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ApprovalGroupDtls> approvalGroupDtlsList;
}
