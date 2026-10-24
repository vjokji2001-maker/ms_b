package com.smartcare.hrms.common.entity.master.attendance.shift;

import java.io.Serial;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.enumeration.LateMarkType;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "shift_lt_rl")
public class LateMarkRule extends BaseEntityId {

    @Serial
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private ShiftTemplate shift;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private LateMarkType type;

    @Column(name = "from_min")
    private Integer fromTime;

    @Column(name = "to_min")
    private Integer toTime;

    @Column(name = "lt_mrk_cnt")
    private Double markCount;

    @Column(name = "ttl_lt_mrk_cnt")
    private Double totalMarkCount;

    @Transient
    private boolean remove;
}

