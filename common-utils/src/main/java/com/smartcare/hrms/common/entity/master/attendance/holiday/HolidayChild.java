package com.smartcare.hrms.common.entity.master.attendance.holiday;

import java.time.LocalDate;

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
@Table(name="holiday_chld")
public class HolidayChild extends BaseEntityId{


	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holiday_id")
	private HolidayParent holiday;
	
	@Column(name="name")
	private String name;
	
	@Column(name="date")
	private LocalDate date;
	
	@Column(name="is_checked")
	private boolean checked;
	
	@Transient
	private boolean remove;
	
	
}
