package com.smartcare.hrms.common.repository.attendance.shift;

import com.smartcare.hrms.common.bean.attendance.ShiftDetailsBean;
import com.smartcare.hrms.common.entity.attendance.shift.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {

    List<EmployeeShift> findByEmployeeIdAndDate(UUID employeeId, LocalDate date);


    @Query("SELECT es FROM EmployeeShift es " +
            "WHERE es.employee.id IN :empIds AND es.date IN :dates")
    List<EmployeeShift> findAllByEmployeeIdInAndDateIn(
            @Param("empIds") List<UUID> empIds,
            @Param("dates") List<LocalDate> dates);


    @Query(value="SELECT es.id AS empShiftId,es.shift_id AS shiftId,es.emp_id AS employeeId, es.shift_strt_time AS shiftStartTime ,date AS shiftDate, " +
            " es.shift_end_time AS shiftEndTime,es.grace_time_mins AS graceTimeMin,st.fh_out_time AS firstHalfOutTime,st.end_before AS endBefore,st.in_time AS shiftInTime, " +
            "  st.out_time AS shiftOutTime,st.sh_in_time AS secondHalfInTime,COALESCE(late.from_min,  bothrule.from_min) AS lateInFromMin, COALESCE(late.to_min,    bothrule.to_min) AS lateInToMin," +
            " COALESCE(late.lt_mrk_cnt,bothrule.lt_mrk_cnt)  AS lateInMarkCount, COALESCE(early.from_min,  bothrule.from_min) AS earlyOutFromMin, "+
            " COALESCE(early.to_min,    bothrule.to_min) AS earlyOutToMin,COALESCE(early.lt_mrk_cnt,bothrule.lt_mrk_cnt) AS earlyOutMarkCount,st.half_day AS halfDayAllowed, "+
            " st.half_rate AS halfRate, st.full_rate AS fullRate,st.night_shift AS nightShiftExist "+
            " FROM emp_shift es  " +
            " INNER JOIN shift_tmplt st ON st.id =es.shift_id "+
            " LEFT JOIN shift_lt_rl late ON late.shift_id = st.id AND late.type = 'LATE_IN' "+
            " LEFT JOIN shift_lt_rl early ON early.shift_id = st.id AND early.type = 'EARLY_OUT' "+
            " LEFT JOIN shift_lt_rl bothrule ON bothrule.shift_id = st.id AND bothrule.type = 'BOTH' "+
            "WHERE es.emp_id=:empId AND es.date BETWEEN :fromDate AND :toDate  ",nativeQuery = true)
    List<ShiftDetailsBean> getShiftDetails(@Param("empId") String empId,@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    @Query("SELECT es FROM EmployeeShift es " +
            "WHERE es.date BETWEEN :fromDate AND :toDate " +
            "AND es.employee.id IN (:empIds)")
    List<EmployeeShift> findShiftsByEmpIdsAndDateRange(
            @Param("empIds") List<UUID> empIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
