package com.smartcare.hrms.common.repository.master.attendance.shift;

import com.smartcare.hrms.common.bean.attendance.EmployeeShiftDetailBean;
import com.smartcare.hrms.common.entity.master.attendance.shift.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate,Long> {

    List<ShiftTemplate> findByNameContainingIgnoreCase(String name);

    ShiftTemplate findByCode(String code);

    @Query(value = "SELECT " +
            "in_time AS shiftInTime, " +
            "out_time AS shiftOutTime, " +
            "in_time AS empInTime, " +
            "out_time AS empOutTime, " +
            "fh_out_time AS firstHalfOutTime, " +
            "sh_in_time AS secondHalfInTime, " +
            "buffer_min AS graceTimeMins, " +
            "end_before AS cutoffHr "+
            "FROM shift_tmplt WHERE id = :shiftId",
            nativeQuery = true)
    EmployeeShiftDetailBean findShiftDataById(@Param("shiftId") Long shiftId);

    @Query(value = "SELECT " +
            "id AS shiftId, " +
            "in_time AS shiftInTime, " +
            "out_time AS shiftOutTime, " +
            "in_time AS empInTime, " +
            "out_time AS empOutTime, " +
            "fh_out_time AS firstHalfOutTime, " +
            "sh_in_time AS secondHalfInTime, " +
            "buffer_min AS graceTimeMins, " +
            "end_before AS cutoffHr " +
            "FROM shift_tmplt WHERE id IN (:shiftIds)",
            nativeQuery = true)
    List<EmployeeShiftDetailBean> findAllShiftDataByIds(@Param("shiftIds") List<Long> shiftIds);

    Boolean existsByCode(String code);
}
