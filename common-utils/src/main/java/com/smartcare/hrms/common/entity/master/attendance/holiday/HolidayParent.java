package com.smartcare.hrms.common.entity.master.attendance.holiday;

import java.time.Year;
import java.util.List;

import com.smartcare.hrms.common.entity.BaseEntityId;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name="holiday_prnt")
public class HolidayParent extends BaseEntityId {


	@Column(name="name")
	private String name;
	
	@Column(name="code")
	private String code;
	
	@Column(name="year")
	private Year year;
	
	@Column(name="total_days")
	private Double totalDays;
	
	@OneToMany(mappedBy = "holiday", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<HolidayChild> holidayChild ;
}
