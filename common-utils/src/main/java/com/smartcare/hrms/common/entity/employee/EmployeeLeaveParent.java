package com.smartcare.hrms.common.entity.employee;


import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplate;
import com.smartcare.hrms.common.entity.master.attendance.period.YearCycle;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="emp_leave_prnt")
public class EmployeeLeaveParent extends BaseEntityId {


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false, unique = true)
    private Employee employee;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id", nullable = false)
    private YearCycle yearCycle;

    @Column(name = "balance")
    private String balance;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_tmplt_id", nullable = false)
    private LeaveTemplate leaveTemplate;

    @OneToMany(mappedBy = "employeeLeave", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmployeeLeaveDtls> employeeLeaveDtls;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmployeeLeaveParent)) return false;
        EmployeeLeaveParent that = (EmployeeLeaveParent) o;
        return id != null && id.equals(that.id); // ONLY id
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // ONLY
    }
}
