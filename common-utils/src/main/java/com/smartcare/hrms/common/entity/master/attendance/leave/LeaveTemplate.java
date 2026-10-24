package com.smartcare.hrms.common.entity.master.attendance.leave;

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
@Table(name="leave_tmplt")
public class LeaveTemplate extends BaseEntityId{

	@Column(name="name")
    private String name;
    @Column(name="code")
    private String code;
   @Column(name="total_leave_days")
    private Double totalLeaveDays;

  @OneToMany(mappedBy = "leave", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<LeaveTemplateDtls> leaveTemplateDtls;
}
