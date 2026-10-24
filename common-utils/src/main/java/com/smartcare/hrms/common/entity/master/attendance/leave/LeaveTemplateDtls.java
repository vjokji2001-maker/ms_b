package com.smartcare.hrms.common.entity.master.attendance.leave;

import com.smartcare.hrms.common.entity.BaseEntityId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="leave_tmplt_dtls")
public class LeaveTemplateDtls extends BaseEntityId {

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="leave_id")
	private LeaveTemplate leave;
	@Column(name="leave_name_id")
	private Long  leaveNameId;
	@Column(name="yrly_count")
	private Double yearlyCount;
	@Column(name="qtrly_count")
	private Double quaterlyCount;
	@Column(name="mnthly_count")
	private Double  monthlyCount;
	@Column(name="pst_days")
	private Double pastDays;
	@Column(name="future_days")
	private Double futureDays;
	@Column(name="half_day_allowed")
	private boolean halfDayAllowed;
	@Transient
	private boolean remove;
}
