package com.smartcare.hrms.payroll.controller.finance;

import com.smartcare.hrms.payroll.config.PayrollBaseController;
import com.smartcare.hrms.payroll.dto.finance.BankBranchDTO;
import com.smartcare.hrms.payroll.dto.finance.BankDTO;
import com.smartcare.hrms.payroll.dto.finance.IfscDetails;
import com.smartcare.hrms.payroll.service.finance.BankBranchService;
import com.smartcare.hrms.payroll.service.finance.BankService;
import com.smartcare.hrms.payroll.service.finance.IfscService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/bank")
@CrossOrigin
@Log4j2
public class BankController extends PayrollBaseController {

    private @Autowired IfscService ifscService;
    private @Autowired BankService bankService;
    private @Autowired BankBranchService bankBranchService;

    @GetMapping(value = "/get/by/{ifscCode}")
    public ResponseEntity<?> getByIfsc(@RequestHeader HttpHeaders headers, @PathVariable("ifscCode") String ifscCode) {
        try {
            IfscDetails ifscDetails = ifscService.fetchIfscDetails(ifscCode.toUpperCase(Locale.ROOT));
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("bank.details.fetch.success"), ifscDetails, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("bank.details.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@RequestHeader HttpHeaders headers, @RequestBody BankDTO dto) {
        try {
            bankService.createBank(dto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("bank.details.create.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("bank.details.create.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/all")
    public ResponseEntity<?> getAll(@RequestHeader HttpHeaders headers) {
        try {

            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("bank.details.fetch.success"), bankService.getAllBanks(), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("bank.details.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/get/{id}")
    public ResponseEntity<?> getById(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
        try {

            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("bank.details.fetch.success"), bankService.getBankById(id), HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("bank.details.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateBank(@RequestHeader HttpHeaders headers,@PathVariable("id") Long id, @RequestBody BankDTO dto) {
        try {
            bankService.updateBank(id,dto);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("bank.details.update.success"),null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("bank.details.update.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping(value = "/deactivate/{id}")
    public ResponseEntity<?> deactivateBank(@RequestHeader HttpHeaders headers,@PathVariable("id") Long id) {
        try {
            bankService.deactivateBank(id);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("bank.details.deactivate.success"),null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("bank.details.deactivate.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/create/branch")
    public ResponseEntity<?> createBranch(@RequestHeader HttpHeaders headers, @RequestBody BankBranchDTO bankBranchDTO) {
        try {
            bankBranchService.createBankBranch( bankBranchDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("branch.details.create.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("branch.details.create.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping(value = "/update/branch/{id}")
    public ResponseEntity<?> updateBranch(@RequestHeader HttpHeaders headers,@PathVariable("id") Long id,@RequestBody BankBranchDTO bankBranchDTO) {
        try {
            bankBranchService.updateBankBranch(id,bankBranchDTO);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("branch.details.update.success"), null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("branch.details.update.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/get/branch/{id}")
    public ResponseEntity<?> getBranchById(@RequestHeader HttpHeaders headers, @PathVariable("id") Long id) {
        try {
            BankBranchDTO branchByIfsc = bankBranchService.getBranchById(id);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("branch.details.fetch.success"), branchByIfsc, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("branch.details.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/get/by/ifsc/{ifscCode}")
    public ResponseEntity<?> getBranchByIfscCode(@RequestHeader HttpHeaders headers, @PathVariable("ifscCode") String ifscCode) {
        try {
            BankBranchDTO branchByIfsc = bankBranchService.getBranchByIfsc(ifscCode);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("branch.details.fetch.success"), branchByIfsc, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("branch.details.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/all/branches/{bankId}")
    public ResponseEntity<?> getBranch(@RequestHeader HttpHeaders headers, @PathVariable("bankId") Long bankId) {
        try {
            List<BankBranchDTO> branchesByBank = bankBranchService.getBranchesByBank(bankId);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("branch.details.fetch.success"), branchesByBank, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("branch.details.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/all/branches")
    public ResponseEntity<?> getAllBranch(@RequestHeader HttpHeaders headers) {
        try {
            List<BankBranchDTO> branchesByBank = bankBranchService.getAllBranches();
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("branch.details.fetch.success"), branchesByBank, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("branch.details.fetch.failed"), HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping(value = "/deactivate/branch/{id}")
    public ResponseEntity<?> deactivateBankBranch(@RequestHeader HttpHeaders headers,@PathVariable("id") Long id) {
        try {
            bankBranchService.deactivateBankBranch(id);
            return responseGenerator.postSuccessResponse(headers, messageResource.getEnglishMessage("branch.details.deactivate.success"),null, HttpStatus.OK);
        } catch (Exception e) {
            String cause = e.getLocalizedMessage();
            return responseGenerator.errorResponse(headers, cause, messageResource.getEnglishMessage("branch.details.deactivate.failed"), HttpStatus.BAD_REQUEST);
        }
    }
}
