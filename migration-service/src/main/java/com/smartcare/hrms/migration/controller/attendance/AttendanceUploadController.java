package com.smartcare.hrms.migration.controller.attendance;

import com.smartcare.hrms.migration.helper.attendance.AttendanceTemplateGenerator;
import com.smartcare.hrms.migration.service.attendance.AttendanceExcelHelper;
import com.smartcare.hrms.migration.service.attendance.AttendanceUploadService;
import com.smartcare.hrms.migration.service.audit.UploadAuditService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
@CrossOrigin("*")
@Log4j2
public class AttendanceUploadController {

    @Autowired
    private UploadAuditService uploadAuditService;

    @Autowired
    private AttendanceUploadService attendanceUploadService;



    @GetMapping("/get/template")
    public ResponseEntity<ByteArrayResource> downloadTemplate() throws IOException {

        byte[] template = AttendanceExcelHelper.generateTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_upload_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(template.length)
                .body(new ByteArrayResource(template));
    }



    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(
            @RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file) throws IOException {

        String userId = headers.getFirst("userid");

        try {
            attendanceUploadService.uploadAttendance(file);

            uploadAuditService.save(
                    UploadAuditService.TYPE_ATTENDANCE_UPLOAD,
                    file.getOriginalFilename(),
                    userId,
                    UploadAuditService.STATUS_SUCCESS
            );

            return ResponseEntity.ok("Attendance uploaded successfully");

        } catch (Exception ex) {

            uploadAuditService.save(
                    UploadAuditService.TYPE_ATTENDANCE_UPLOAD,
                    file.getOriginalFilename(),
                    userId,
                    UploadAuditService.STATUS_FAILED
            );

            throw ex;
        }
    }
    @PostMapping(value = "/upload/attendance/excel/count", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadAttendanceCountExcel(
            @RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file) {

        String userId   = headers.getFirst("userid");
        String filename = file.getOriginalFilename();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "FAILED", "error", "File is empty"));
        }

        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "FAILED", "error", "Only .xlsx or .xls files are accepted"));
        }

        try {
            Map<String, Object> result = attendanceUploadService.processExcel(file);

            uploadAuditService.save(
                    UploadAuditService.TYPE_MONTHLY_ATTENDANCE_UPLOAD,
                    filename,
                    userId,
                    UploadAuditService.STATUS_SUCCESS
            );

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            uploadAuditService.save(
                    UploadAuditService.TYPE_MONTHLY_ATTENDANCE_UPLOAD,
                    filename,
                    userId,
                    UploadAuditService.STATUS_FAILED
            );

            return ResponseEntity.badRequest()
                    .body(Map.of("status", "FAILED", "error", e.getMessage()));

        } catch (Exception e) {

            uploadAuditService.save(
                    UploadAuditService.TYPE_MONTHLY_ATTENDANCE_UPLOAD,
                    filename,
                    userId,
                    UploadAuditService.STATUS_FAILED
            );

            log.error("Error processing attendance count excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "ERROR", "error", e.getMessage()));
        }
    }

    @GetMapping("/download/attendance/count/template")
    public ResponseEntity<byte[]> downloadAttendanceCountTemplate() {
        try {
            byte[] templateBytes = AttendanceTemplateGenerator.generate();

            String fileName = "attendance_upload_template_"
                    + YearMonth.now().toString()   // e.g. 2024-06
                    + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(templateBytes.length)
                    .body(templateBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

