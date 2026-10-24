package com.smartcare.hrms.common.entity.payroll.compensation.employee;

import com.smartcare.hrms.common.entity.BaseEntityUUID;
import com.smartcare.hrms.common.enumeration.CalculationType;
import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.entity.payroll.compensation.component.ComponentMaster;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "emp_compensation_dtls")
public class EmpCompensationDtls extends BaseEntityUUID {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_cmpnstn_id")
    private EmpCompensationHdr empCompensationHdr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private ComponentMaster componentMaster;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private ComponentType componentType;;

    @Enumerated(EnumType.STRING)
    @Column(name = "cal_typ", nullable = false)
    private CalculationType calculationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "formula_json", columnDefinition = "longtext")
    private Formula formula;

    @Column(name = "amt")
    private Double amount;

    @Column(name = "seq")
    private Integer sequence; // For ordering components in the compensation structure

}
