package com.smartcare.hrms.common.entity.master.attendance.weekOff;

import java.io.Serial;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.enumeration.DayOfWeek;
import com.smartcare.hrms.common.enumeration.WeekNo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wk_off_dtl")
public class WeekOffDetail extends BaseEntityId {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wk_off_id")
	private WeekOffParent weekOff;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "day")
	private DayOfWeek day;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "week_no")
	private WeekNo weekNo;
		
	@Transient
    private boolean remove;
}
