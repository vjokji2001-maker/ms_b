package com.smartcare.hrms.payroll.entity.finance;

import com.smartcare.hrms.common.entity.BaseEntityId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bank extends BaseEntityId {

    @Column(name = "name")
    private String name;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "code")
    private String code;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}