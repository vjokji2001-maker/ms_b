package com.smartcare.hrms.common.entity.master.attendance.shift;


import com.smartcare.hrms.common.entity.BaseEntityId;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.time.LocalTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "shift_tmplt")
public class ShiftTemplate extends BaseEntityId {

    @Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "night_shift")
    private Boolean nightShift;

    @Column(name = "half_day")
    private Boolean halfDay;

    @Column(name = "hour_base")
    private Boolean hourBased;

    @Column(name = "in_time")
    private LocalTime inTime;

    @Column(name = "out_time")
    private LocalTime outTime;

    @Column(name = "ttl_wrk_hr")
    private LocalTime totalWorkingHours;

    @Column(name = "fh_out_time")
    private LocalTime firstHalfOutTime;

    @Column(name = "sh_in_time")
    private LocalTime secondHalfInTime;

    @Column(name = "ttl_half_day_hr")
    private LocalTime totalHalfDayHours;

    @Column(name = "buffer_min")
    private Integer bufferTime;

    @Column(name = "end_before")
    private Integer cutOffTime;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LateMarkRule> lateMarkRules;

    @Column(name = "half_rate")
    private Double halfRate;

    @Column(name = "full_rate")
    private Double fullRate;


}
