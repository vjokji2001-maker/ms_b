package com.smartcare.hrms.common.entity.payroll.compensation.component;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.enumeration.ComponentType;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="compensation_head")
public class ComponentMaster extends BaseEntityId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private ComponentType type;
}
