package com.smartcare.hrms.attendance.dao.attendance;

import com.smartcare.hrms.attendance.dto.attendance.MyAttendanceDashboardDTO;
import com.smartcare.hrms.common.pagination.ColumnMeta;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MyAttendanceDashboardDAO {
    @Autowired
    private EntityManager entityManager;

    private static List<ColumnMeta> columns = new ArrayList<>(List.of(new ColumnMeta[]{
            new ColumnMeta("Date", "date"),
            new ColumnMeta("Day", "day"),
            new ColumnMeta("Shift Code", "shiftCode"),
            new ColumnMeta("In Time", "inTime"),
            new ColumnMeta("Out Time", "outTime"),
            new ColumnMeta("Day Type", "dayType"),
            new ColumnMeta("Day Type 1", "dayType1"),
            new ColumnMeta("Portion", "portion"),
            new ColumnMeta("Late In Mark", "lateInMark"),
            new ColumnMeta("Late Out Mark", "lateOutMark"),
            new ColumnMeta("Total Marks", "sra.status"),
            new ColumnMeta("Total Hrs", "totalHours"),
            new ColumnMeta("Status", "status"),
    }));
    private final String sqlQuery="";

    public List<MyAttendanceDashboardDTO> getData(LocalDate fromDate,LocalDate toDate,String employeeId){
    return null;
    }
}
