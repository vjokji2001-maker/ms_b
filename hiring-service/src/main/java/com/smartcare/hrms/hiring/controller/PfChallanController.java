package com.smartcare.hrms.hiring.controller;

import com.smartcare.hrms.hiring.dto.ApiResponse;
import com.smartcare.hrms.hiring.dto.PfChallanListDTO;
import com.smartcare.hrms.hiring.dto.PfChallanRequestDTO;
import com.smartcare.hrms.hiring.dto.PfEmployeeDetailsResponseDTO;
import com.smartcare.hrms.hiring.dto.PfEmployeeSummaryDTO;
import com.smartcare.hrms.hiring.dto.PageResponse;
import com.smartcare.hrms.hiring.service.PfChallanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pf")
public class PfChallanController {

    private @Autowired
    PfChallanService challanService;

    @GetMapping("/challans")
    public ApiResponse<PageResponse<PfChallanListDTO>>
    getChallanList(
            @RequestParam(required = false)
            String financialYear,

            @RequestParam(required = false)
            String month,

            @RequestParam(required = false)
            String searchEmployee,

            @RequestParam(defaultValue = "0")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size) {

        PfChallanRequestDTO requestDTO =
                new PfChallanRequestDTO();

        requestDTO.setFinancialYear(
                financialYear
        );

        requestDTO.setMonth(
                month
        );

        requestDTO.setSearchEmployee(
                searchEmployee
        );

        requestDTO.setPage(
                page
        );

        requestDTO.setSize(
                size
        );

        PageResponse<PfChallanListDTO> result =
                challanService.getChallanList(
                        requestDTO
                );

        return new ApiResponse<>(
                true,
                "PF challan list fetched successfully",
                result
        );
    }

    @PostMapping("/challans/generate")
    public ApiResponse<PfChallanListDTO>
    generateChallan(
            @RequestBody
            PfChallanRequestDTO requestDTO) {

        PfChallanListDTO result =
                challanService.generateChallan(
                        requestDTO
                );

        return new ApiResponse<>(
                true,
                "PF challan generated successfully",
                result
        );
    }

    @GetMapping("/empinfo")
    public ApiResponse<PfEmployeeDetailsResponseDTO>
    getEmployeeDetails(
            @RequestParam(required = false)
            String financialYear,

            @RequestParam(required = false)
            String month,

            @RequestParam(required = false)
            String searchEmployee,

            @RequestParam(defaultValue = "0")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size) {

        PfChallanRequestDTO requestDTO =
                new PfChallanRequestDTO();

        requestDTO.setFinancialYear(
                financialYear
        );

        requestDTO.setMonth(
                month
        );

        requestDTO.setSearchEmployee(
                searchEmployee
        );

        requestDTO.setPage(
                page
        );

        requestDTO.setSize(
                size
        );

        PfEmployeeDetailsResponseDTO result =
                challanService.getEmployeeDetails(
                        requestDTO
                );

        return new ApiResponse<>(
                true,
                "PF employee details fetched successfully",
                result
        );
    }

    @GetMapping("/summary")
    public ApiResponse<PfEmployeeSummaryDTO>
    getSummary(
            @RequestParam(required = false)
            String financialYear,

            @RequestParam(required = false)
            String month) {

        PfEmployeeSummaryDTO result =
                challanService.getSummary(
                        financialYear,
                        month
                );

        return new ApiResponse<>(
                true,
                "PF summary fetched successfully",
                result
        );
    }
}