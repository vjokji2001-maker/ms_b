package com.smartcare.hrms.common.entity.employee;


import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.entity.master.attendance.approvalgroup.ApprovalGroup;
import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplate;
import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffParent;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="emp_config")
@Builder
public class EmployeeConfig  extends BaseEntityId{

	/**
	 * 
	 */
	@Serial
    private static final long serialVersionUID = 1L;

	@OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false, unique = true)
	 private Employee employee;

	@OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "week_off_id")
	private WeekOffParent weekOffId;

	@OneToOne (fetch = FetchType.LAZY)
	@JoinColumn(name="holiday_clndr_id")
	private HolidayParent holidayParent;

	@OneToOne (fetch = FetchType.LAZY)
	@JoinColumn(name="aprvl_grp_id")
	private ApprovalGroup approvalId;
	

}
