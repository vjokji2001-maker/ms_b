package com.smartcare.hrms.migration.controller.employee;

import com.smartcare.hrms.migration.dto.employee.EmployeeUploadErrorDTO;
import com.smartcare.hrms.migration.dto.employee.EmployeeUploadResultDTO;
import com.smartcare.hrms.migration.service.audit.UploadAuditService;
import com.smartcare.hrms.migration.service.employee.EmployeeExcelUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/employee")
public class EmployeeUploadController {

    private @Autowired EmployeeExcelUploadService employeeUploadService;

    private @Autowired UploadAuditService uploadAuditService;

    // ─── Download Template ────────────────────────────────────────────────────────

    @GetMapping("/get/template")
    public ResponseEntity<ByteArrayResource> downloadTemplate() throws IOException {
        byte[] template = com.smartcare.hrms.migration.service.employee.EmployeeExcelHelper.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_upload_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(template.length)
                .body(new ByteArrayResource(template));
    }

    // ─── Upload Excel ─────────────────────────────────────────────────────────────

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeUploadResultDTO> upload(@RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file ) throws IOException {
        String userId= headers.getFirst("userid");

        try {
            EmployeeUploadResultDTO result = employeeUploadService.uploadFromExcel(file);
            uploadAuditService.save(UploadAuditService.TYPE_EMPLOYEE, file.getOriginalFilename(), userId, UploadAuditService.STATUS_SUCCESS);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            uploadAuditService.save(UploadAuditService.TYPE_EMPLOYEE, file.getOriginalFilename(), userId, UploadAuditService.STATUS_FAILED);

            throw ex;
        }
    }
}
