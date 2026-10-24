package com.smartcare.hrms.payroll.service.form16;

import com.smartcare.hrms.payroll.dao.Form16DashboardDAO;
import com.smartcare.hrms.payroll.dto.form16.Form16DashboardDTO;
import com.smartcare.hrms.payroll.dto.form16.Form16DashboardRequestDTO;
import com.smartcare.hrms.payroll.dto.form16.Form16DTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Form16Service {

    private @Autowired Form16DashboardDAO form16DashboardDAO;

    public List<Form16DTO> getForm16EmployeeData(
            Form16DashboardRequestDTO requestDTO) {

        List<Form16DTO> data =
                form16DashboardDAO.getForm16EmployeeData(
                        requestDTO
                );

        int page =
                requestDTO.getPage() == null
                        ? 0
                        : requestDTO.getPage();

        int size =
                requestDTO.getSize() == null
                        ? 10
                        : requestDTO.getSize();

        int fromIndex = page * size;

        if (fromIndex >= data.size()) {
            return List.of();
        }

        int toIndex =
                Math.min(
                        fromIndex + size,
                        data.size()
                );

        return data.subList(
                fromIndex,
                toIndex
        );
    }

    public Form16DTO getForm16EmployeeDetails(
            String employeeId,
            String financialYear) {

        return form16DashboardDAO
                .getForm16EmployeeDetails(
                        employeeId,
                        financialYear
                );
    }

    public Form16DashboardDTO getDashboardSummary(
            Form16DashboardRequestDTO requestDTO) {

        List<Form16DTO> data =
                form16DashboardDAO.getForm16EmployeeData(
                        requestDTO
                );

        Form16DashboardDTO dashboardDTO =
                new Form16DashboardDTO();

        dashboardDTO.setFinancialYear(
                requestDTO.getFinancialYear()
        );

        long totalEmployees = data.size();

        long form16Generated = data.stream()
                .filter(x -> Boolean.TRUE.equals(
                        x.getForm16Generated()
                ))
                .count();

        long downloaded = data.stream()
                .filter(x -> Boolean.TRUE.equals(
                        x.getForm16Downloaded()
                ))
                .count();

        long pending =
                totalEmployees - form16Generated;

        dashboardDTO.setTotalEmployees(
                totalEmployees
        );

        dashboardDTO.setForm16Generated(
                form16Generated
        );

        dashboardDTO.setPending(
                pending
        );

        dashboardDTO.setDownloaded(
                downloaded
        );

        return dashboardDTO;
    }
}