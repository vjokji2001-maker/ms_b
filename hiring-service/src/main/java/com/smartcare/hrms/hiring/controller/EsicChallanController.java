package com.smartcare.hrms.hiring.controller;

import com.smartcare.hrms.hiring.dto.*;
import com.smartcare.hrms.hiring.service.EsicChallanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/esic")
public class EsicChallanController {

    private @Autowired
    EsicChallanService challanService;

    @GetMapping("/challans")
    public ApiResponse<PageResponse<EsicChallanListDTO>>
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

        EsicChallanRequestDTO requestDTO =
                new EsicChallanRequestDTO();

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

        PageResponse<EsicChallanListDTO> result =
                challanService.getChallanList(
                        requestDTO
                );

        return new ApiResponse<>(
                true,
                "ESIC challan list fetched successfully",
                result
        );
    }

    @PostMapping("/challans/generate")
    public ApiResponse<EsicChallanListDTO>
    generateChallan(
            @RequestBody
            EsicChallanRequestDTO requestDTO) {

        EsicChallanListDTO result =
                challanService.generateChallan(
                        requestDTO
                );

        return new ApiResponse<>(
                true,
                "ESIC challan generated successfully",
                result
        );
    }

    @GetMapping("/empinfo")
    public ApiResponse<EsicEmployeeDetailsResponseDTO>
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

        EsicChallanRequestDTO requestDTO =
                new EsicChallanRequestDTO();

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

        EsicEmployeeDetailsResponseDTO result =
                challanService.getEmployeeDetails(
                        requestDTO
                );

        return new ApiResponse<>(
                true,
                "ESIC employee details fetched successfully",
                result
        );
    }

    @GetMapping("/summary")
    public ApiResponse<EsicEmployeeSummaryDTO>
    getSummary(
            @RequestParam(required = false)
            String financialYear,

            @RequestParam(required = false)
            String month) {

        EsicEmployeeSummaryDTO result =
                challanService.getSummary(
                        financialYear,
                        month
                );

        return new ApiResponse<>(
                true,
                "ESIC summary fetched successfully",
                result
        );
    }
}