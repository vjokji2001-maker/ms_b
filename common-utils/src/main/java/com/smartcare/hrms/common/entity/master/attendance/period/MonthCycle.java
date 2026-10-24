package com.smartcare.hrms.common.entity.master.attendance.period;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.entity.master.employment.Grade;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "month_cycle")
public class MonthCycle extends BaseEntityId {

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "start_date")
    private Integer startDate;

    @Column(name = "end_date")
    private Integer endDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;
}

