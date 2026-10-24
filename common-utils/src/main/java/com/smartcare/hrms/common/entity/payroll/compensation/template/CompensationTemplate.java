package com.smartcare.hrms.common.entity.payroll.compensation.template;

import com.smartcare.hrms.common.entity.BaseEntityId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "compensation_template")
public class CompensationTemplate extends BaseEntityId {

    @Column(name = "name")
    private String name;

    @Column(name="code")
    private String code;

    @Column(name="grs_min_amount")
    private Double grossMinAmount;

    @Column(name="grs_max_amount")
    private Double grossMaxAmount;

    @Column(name="grade_id")
    private Long  gradeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "component_dtls", columnDefinition = "longtext")
    private List<CompensationTemplateComponent> componentList;
}
