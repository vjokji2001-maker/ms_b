package com.smartcare.hrms.report.service.employee;

import com.smartcare.hrms.report.bean.employee.HeadcountReportBean;
import com.smartcare.hrms.report.modal.dto.employee.EmployeeReposrtSearchDTO;
import com.smartcare.hrms.report.modal.dto.employee.HeadcountReportDTO;
import com.smartcare.hrms.report.repo.HeadcountReportRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HeadcountReportService {
    @Autowired
    private HeadcountReportRepository repository;


    public List<HeadcountReportDTO> getHeadcountReport(EmployeeReposrtSearchDTO searchDTO) {

        List<HeadcountReportBean> list = repository.getHeadcountReport(
                searchDTO.getFromDate(),
                searchDTO.getToDate(),
                searchDTO.getCompanyId(),
                searchDTO.getBranchId(),
                searchDTO.getDepartmentId(),
                searchDTO.getWorkLocationId()
        );
        return list.stream().map(bean ->

                HeadcountReportDTO.builder()
                        .departmentCode(bean.getDepartmentCode())
                        .departmentName(bean.getDepartmentName())
                        .currentHeadcount(bean.getCurrentHeadcount())
                        .activeEmployees(bean.getActiveEmployees())
                        .probationEmployees(bean.getProbationEmployees())
                        .confirmedEmployees(bean.getConfirmedEmployees())
                        .permanentEmployees(bean.getPermanentEmployees())
                        .contractEmployees(bean.getContractEmployees())
                        .maleEmployees(bean.getMaleEmployees())
                        .femaleEmployees(bean.getFemaleEmployees())
                        .newJoiners(bean.getNewJoiners())
                        .employeesOnNoticePeriod(bean.getEmployeesOnNoticePeriod())
                        .resignedEmployees(bean.getResignedEmployees())
                        .terminatedEmployees(bean.getTerminatedEmployees())
                        .exitedEmployees(bean.getExitedEmployees())
                        .branchName(bean.getBranchName())
                        .workLocation(bean.getWorkLocation())
                        .lastUpdatedDate(bean.getLastUpdatedDate())
                        .build()

        ).toList();
    }

    public void exportHeadcountExcel(EmployeeReposrtSearchDTO searchDTO, HttpServletResponse response) throws Exception {

        List<HeadcountReportDTO> data = getHeadcountReport(searchDTO);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Headcount Report");

        CellStyle headerStyle = workbook.createCellStyle();

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row header = sheet.createRow(0);

        int col = 0;

        createHeaderCell(header, col++, "Sr No", headerStyle);
        createHeaderCell(header, col++, "Department Code", headerStyle);
        createHeaderCell(header, col++, "Department Name", headerStyle);
        createHeaderCell(header, col++, "Current Headcount", headerStyle);
        createHeaderCell(header, col++, "Active Employees", headerStyle);
        createHeaderCell(header, col++, "Probation Employees", headerStyle);
        createHeaderCell(header, col++, "Confirmed Employees", headerStyle);
        createHeaderCell(header, col++, "Permanent Employees", headerStyle);
        createHeaderCell(header, col++, "Contract Employees", headerStyle);
        createHeaderCell(header, col++, "Male Employees", headerStyle);
        createHeaderCell(header, col++, "Female Employees", headerStyle);
        createHeaderCell(header, col++, "New Joiners", headerStyle);
        createHeaderCell(header, col++, "Notice Period Employees", headerStyle);
        createHeaderCell(header, col++, "Resigned Employees", headerStyle);
        createHeaderCell(header, col++, "Terminated Employees", headerStyle);
        createHeaderCell(header, col++, "Exited Employees", headerStyle);
        createHeaderCell(header, col++, "Branch", headerStyle);
        createHeaderCell(header, col++, "Work Location", headerStyle);
        createHeaderCell(header, col++, "Last Updated Date", headerStyle);

        int rowIndex = 1;
        int srNo = 1;

        for (HeadcountReportDTO dto : data) {

            Row row = sheet.createRow(rowIndex++);
            col = 0;

            row.createCell(col++).setCellValue(srNo++);
            row.createCell(col++).setCellValue(dto.getDepartmentCode());
            row.createCell(col++).setCellValue(dto.getDepartmentName());
            row.createCell(col++).setCellValue(dto.getCurrentHeadcount());
            row.createCell(col++).setCellValue(dto.getActiveEmployees());
            row.createCell(col++).setCellValue(dto.getProbationEmployees());
            row.createCell(col++).setCellValue(dto.getConfirmedEmployees());
            row.createCell(col++).setCellValue(dto.getPermanentEmployees());
            row.createCell(col++).setCellValue(dto.getContractEmployees());
            row.createCell(col++).setCellValue(dto.getMaleEmployees());
            row.createCell(col++).setCellValue(dto.getFemaleEmployees());
            row.createCell(col++).setCellValue(dto.getNewJoiners());
            row.createCell(col++).setCellValue(dto.getEmployeesOnNoticePeriod());
            row.createCell(col++).setCellValue(dto.getResignedEmployees());
            row.createCell(col++).setCellValue(dto.getTerminatedEmployees());
            row.createCell(col++).setCellValue(dto.getExitedEmployees());
            row.createCell(col++).setCellValue(dto.getBranchName());
            row.createCell(col++).setCellValue(dto.getWorkLocation());
            row.createCell(col++).setCellValue(dto.getLastUpdatedDate().toString());
        }

        for (int i = 0; i < 19; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=headcount_report.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void createHeaderCell(Row row, int col, String value, CellStyle style) {

        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
