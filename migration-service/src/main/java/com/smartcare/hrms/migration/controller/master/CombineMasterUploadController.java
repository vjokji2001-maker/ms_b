package com.smartcare.hrms.migration.controller.master;

import com.smartcare.hrms.migration.dto.master.attendance.AttendanceMasterUploadResultDTO;
import com.smartcare.hrms.migration.dto.master.employment.CombinedMasterUploadResultDTO;
import com.smartcare.hrms.migration.service.audit.UploadAuditService;
import com.smartcare.hrms.migration.service.matser.attendance.AttendanceMasterExcelHelper;
import com.smartcare.hrms.migration.service.matser.attendance.AttendanceMasterUploadService;
import com.smartcare.hrms.migration.service.matser.employeement.CombinedMasterExcelHelper;
import com.smartcare.hrms.migration.service.matser.employeement.CombinedMasterUploadService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/master")
@CrossOrigin("*")
@Log4j2
public class CombineMasterUploadController {

    private @Autowired CombinedMasterUploadService combinedMasterUploadService;
    private @Autowired AttendanceMasterUploadService attendanceMasterUploadService;
    private @Autowired UploadAuditService uploadAuditService;

    @PostMapping(value = "/employment/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CombinedMasterUploadResultDTO> uploadEmploymentData(@RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file) throws IOException {
        String userId= headers.getFirst("userid");
        try {

            CombinedMasterUploadResultDTO result = combinedMasterUploadService.uploadFromExcel(file);

            uploadAuditService.save( UploadAuditService.TYPE_EMPLOYMENT_MASTER, file.getOriginalFilename(),userId,UploadAuditService.STATUS_SUCCESS);
            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            uploadAuditService.save( UploadAuditService.TYPE_EMPLOYMENT_MASTER,file.getOriginalFilename(),userId,UploadAuditService.STATUS_FAILED);
            throw ex;
        }
    }

    @GetMapping("/employment/get/template")
    public ResponseEntity<ByteArrayResource> downloadEmploymentTemplate() throws IOException {
        byte[] templateBytes = CombinedMasterExcelHelper.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=master_upload_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(templateBytes.length)
                .body(new ByteArrayResource(templateBytes));
    }

    @GetMapping("/attendance/get/template")
    public ResponseEntity<ByteArrayResource> downloadAttendanceTemplate() throws IOException {
        byte[] template = AttendanceMasterExcelHelper.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_master_upload_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(template.length)
                .body(new ByteArrayResource(template));
    }

    // ─── Upload Excel ─────────────────────────────────────────────────────────────

    @PostMapping(value = "/attendance/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttendanceMasterUploadResultDTO> uploadAttendanceData(@RequestHeader HttpHeaders headers,
                                                                                @RequestParam("file") MultipartFile file) throws IOException {
        String userId= headers.getFirst("userid");
        try {
            AttendanceMasterUploadResultDTO result = attendanceMasterUploadService.uploadFromExcel(file);
            uploadAuditService.save(UploadAuditService.TYPE_ATTENDANCE_MASTER, file.getOriginalFilename(), userId, UploadAuditService.STATUS_SUCCESS);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            uploadAuditService.save(UploadAuditService.TYPE_ATTENDANCE_MASTER, file.getOriginalFilename(), userId, UploadAuditService.STATUS_FAILED);
            throw ex;
        }
    }
}
