package com.smartcare.hrms.common.repository.master.attendance.shift;

import com.smartcare.hrms.common.entity.master.attendance.shift.LateMarkRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LateMarkRuleRepository extends JpaRepository<LateMarkRule,Long> {
}
