package com.smartcare.hrms.common.repository.master.attendance.weekOff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffDetail;

@Repository
public interface WeekOffDetailRepository extends JpaRepository<WeekOffDetail, Long>{

}
