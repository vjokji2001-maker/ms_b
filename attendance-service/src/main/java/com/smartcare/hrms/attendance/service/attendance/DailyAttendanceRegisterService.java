package com.smartcare.hrms.attendance.service.attendance;

import com.smartcare.hrms.attendance.dao.attendance.DailyAttendanceRegisterDAO;
import com.smartcare.hrms.attendance.dto.attendance.AttendanceEntryDTO;
import com.smartcare.hrms.attendance.dto.attendance.DailyAttendanceRegisterDTO;
import com.smartcare.hrms.attendance.dto.attendance.DailyAttendanceRegisterSearchDTO;
import com.smartcare.hrms.attendance.dto.attendance.EmployeeDailyAttendanceSummaryDTO;
import com.smartcare.hrms.attendance.helper.AttendanceStatusResolver;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyAttendanceRegisterService {

    @Autowired
    private DailyAttendanceRegisterDAO dailyAttendanceRegisterDAO;

    public List<EmployeeDailyAttendanceSummaryDTO> getGroupedAttendance(
            DailyAttendanceRegisterSearchDTO dto) {

        // Fetch all records without pagination for grouping
        PaginationInfo paginationInfo = PaginationInfo.builder()
                .currentPage(1)
                .pageSize(Integer.MAX_VALUE)
                .build();
        dto.setPaginationInfo(paginationInfo);

        PaginatedDataResponse<DailyAttendanceRegisterDTO> response =
                dailyAttendanceRegisterDAO.getSavedAttendance(dto);

        if (response == null || CollectionUtils.isEmpty(response.getDataList())) {
            return new ArrayList<>();
        }

        List<DailyAttendanceRegisterDTO> records =
                (List<DailyAttendanceRegisterDTO>) (List<?>) response.getDataList();

        // Group by empId preserving order
        Map<String, List<DailyAttendanceRegisterDTO>> grouped = records.stream()
                .collect(Collectors.groupingBy(
                        DailyAttendanceRegisterDTO::getEmpId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .map(entry -> buildSummaryDTO(entry.getValue()))
                .toList();
    }

    private EmployeeDailyAttendanceSummaryDTO buildSummaryDTO(
            List<DailyAttendanceRegisterDTO> empRecords) {

        DailyAttendanceRegisterDTO first = empRecords.get(0);

        List<AttendanceEntryDTO> attendanceList = empRecords.stream()
                .map(this::buildAttendanceEntry)
                .collect(Collectors.toList());

        return EmployeeDailyAttendanceSummaryDTO.builder()
                .empId(first.getEmpId())
                .empCode(first.getEmpCode())
                .empName(first.getEmpName())
                .attendanceList(attendanceList)
                .build();
    }

    private AttendanceEntryDTO buildAttendanceEntry(DailyAttendanceRegisterDTO dto) {
        return AttendanceEntryDTO.builder()
                .date(dto.getDate())
                .inTime(dto.getInTime())
                .outTime(dto.getOutTime())
                .dayType(dto.getDayType())
                .dayType1(dto.getDayType1())
                .status(AttendanceStatusResolver.resolveStatus(dto.getDayType(), dto.getDayType1()))
                .build();
    }

}
