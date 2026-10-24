package com.smartcare.hrms.report.service.leave;

import com.smartcare.hrms.common.bean.application.leave.ApprovalBean;
import com.smartcare.hrms.common.bean.application.leave.LeaveReportBean;
import com.smartcare.hrms.common.repository.attendance.application.leave.LeaveRequestRepository;
import com.smartcare.hrms.report.modal.dto.attendance.EmployeeSearchDTO;
import com.smartcare.hrms.report.modal.dto.leave.ApprovalDTO;
import com.smartcare.hrms.report.modal.dto.leave.LeaveReportDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeaveReportService {

    private @Autowired LeaveRequestRepository leaveRequestRepository;

    public List<LeaveReportDTO> getReport(EmployeeSearchDTO searchDTO){

        List<LeaveReportBean> parentList= leaveRequestRepository.getLeaveReport(searchDTO.getFromDate(), searchDTO.getToDate()
        , searchDTO.getName(), searchDTO.getCode(), searchDTO.getDepartmentId(), searchDTO.getCompanyId(), searchDTO.getBranchId(),
                searchDTO.getDesignationId(), searchDTO.getCategoryId(), searchDTO.getWorkLocationId(), searchDTO.getGradeId());


        if (parentList == null || parentList.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> requestIds = parentList.stream()
                .map(LeaveReportBean::getRequestId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<ApprovalBean> approvalList = new ArrayList<>();

      if(!requestIds.isEmpty()){
          approvalList= leaveRequestRepository.getApprovalList(requestIds);
      }
        Map<String, LeaveReportDTO> map = new LinkedHashMap<>();

      for(LeaveReportBean row:parentList){
          String requestId= row.getRequestId();

          if(requestId==null) continue;

          LeaveReportDTO dto = map.computeIfAbsent(requestId , k->{
              LeaveReportDTO e = new LeaveReportDTO();
              e.setEmployeeName(row.getEmployeeName());
              e.setEmployeeCode(row.getEmployeeCode());
              e.setDepartment(row.getDepartment());
              e.setLocation(row.getLocation());
              e.setBranch(row.getBranch());
              e.setCategory(row.getCompany());
              e.setDesignation(row.getDesignation());
              e.setGrade(row.getGrade());
              e.setCompany(row.getCategory());
              e.setRequestId(row.getRequestId());
              e.setRequestType(row.getRequestType());
              e.setFromDate(row.getFromDate());
              e.setToDate(row.getToDate());
              e.setTotalDays(row.getTotalDays());
              e.setStatus(row.getStatus());
              e.setReason(row.getReason());
              e.setApprovalList(new ArrayList<>());

              return e;
          });
      }
      for(ApprovalBean approvalBean:approvalList){
          if(approvalBean.getRequestId()==null) continue;

          LeaveReportDTO parent= map.get(approvalBean.getRequestId());

          if(parent==null) continue;

          ApprovalDTO child = new ApprovalDTO();
          child.setApproverName(approvalBean.getApproverName());
          child.setLevel(approvalBean.getLevel());
          child.setRemark(approvalBean.getRemark());
          child.setStatus(approvalBean.getStatus());
          child.setActionDateTime(approvalBean.getActionDateTime());

          parent.getApprovalList().add(child);
      }
        for(LeaveReportDTO dto: map.values()){
            dto.getApprovalList().sort(Comparator.comparing(ApprovalDTO::getLevel));
        }
        return new ArrayList<>(map.values());
    }

    public void exportLeaveExcel(EmployeeSearchDTO searchDTO, HttpServletResponse response) throws  Exception{

        List<LeaveReportDTO> data= getReport(searchDTO);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Leave Report");

        CellStyle headerStyle= workbook.createCellStyle();
        Font font= workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        Row header=sheet.createRow(0);

        String[] columns={
                "Employee Name","Employee Code","Department",
                "Location","Branch","Company", "Designation",
                "Grade","Category",
                "Request Type", "From Date", "To Date", "Total Days", "Status",
                "Approver", "Approval Level", "Remark",  "Approval Status", "Action Date"
        };

        for(int i=0; i<columns.length; i++){
            Cell cell= header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }
        int rowIndex =1;
        for(LeaveReportDTO emp:data){

            if(emp.getApprovalList()==null || emp.getApprovalList().isEmpty()){
                Row row= sheet.createRow(rowIndex++);

                fillCommonData(row, emp);
            }else {
                for(ApprovalDTO approvalDTO: emp.getApprovalList()){
                    Row row= sheet.createRow(rowIndex++);
                    fillCommonData(row, emp);

                    row.createCell(14).setCellValue(approvalDTO.getApproverName()!=null?approvalDTO.getApproverName():"");
                    row.createCell(15).setCellValue(approvalDTO.getLevel()!=null?approvalDTO.getLevel():0);
                    row.createCell(16).setCellValue(approvalDTO.getRemark()!=null?approvalDTO.getRemark():"");
                    row.createCell(17).setCellValue(approvalDTO.getStatus()!=null?approvalDTO.getStatus():"");
                    row.createCell(18).setCellValue(approvalDTO.getActionDateTime()!=null?approvalDTO.getActionDateTime():"");
                }
            }
        }
        for (int i=0; i<columns.length; i++){
            sheet.autoSizeColumn(i);
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=leave_report.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void fillCommonData(Row row, LeaveReportDTO emp){

        row.createCell(0).setCellValue(emp.getEmployeeName()!=null?emp.getEmployeeName():"");
        row.createCell(1).setCellValue(emp.getEmployeeCode()!=null?emp.getEmployeeCode():"");
        row.createCell(2).setCellValue(emp.getDepartment()!=null?emp.getDepartment():"");
        row.createCell(3).setCellValue(emp.getLocation()!=null?emp.getLocation():"");
        row.createCell(4).setCellValue(emp.getBranch()!=null?emp.getBranch():"");
        row.createCell(5).setCellValue(emp.getCompany()!=null?emp.getCompany():"");
        row.createCell(6).setCellValue(emp.getDesignation()!=null?emp.getDesignation():"");
        row.createCell(7).setCellValue(emp.getGrade()!=null?emp.getGrade():"");
        row.createCell(8).setCellValue(emp.getCategory()!=null?emp.getCategory():"");
        row.createCell(9).setCellValue(emp.getRequestType() != null ? emp.getRequestType() : "");
        row.createCell(10).setCellValue(emp.getFromDate() != null ? emp.getFromDate() : "");
        row.createCell(11).setCellValue(emp.getToDate() != null ? emp.getToDate() : "");
        row.createCell(12).setCellValue(emp.getTotalDays() != null ? emp.getTotalDays() : "");
        row.createCell(13).setCellValue(emp.getStatus() != null ? emp.getStatus() : "");


    }
}
