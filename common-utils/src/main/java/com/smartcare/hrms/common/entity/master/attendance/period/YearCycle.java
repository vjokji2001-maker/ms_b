package com.smartcare.hrms.common.entity.master.attendance.period;

import com.smartcare.hrms.common.entity.BaseEntityId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "year_cycle")
public class YearCycle extends BaseEntityId {

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "from_month_year")
    private LocalDate fromMonthYear;

    @Column(name = "to_month_year")
    private LocalDate toMonthYear;

}
