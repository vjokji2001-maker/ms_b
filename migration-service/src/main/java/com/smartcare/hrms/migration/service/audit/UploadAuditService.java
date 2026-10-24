package com.smartcare.hrms.migration.service.audit;

import com.smartcare.hrms.migration.dto.audit.UploadAuditDTO;
import com.smartcare.hrms.migration.enitity.audit.UploadAuditLog;
import com.smartcare.hrms.migration.repository.UploadAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class UploadAuditService {

    public static final String TYPE_EMPLOYEE          = "EMPLOYEE";
    public static final String TYPE_EMPLOYMENT_MASTER   = "EMPLOYMENT_MASTER";
    public static final String TYPE_ATTENDANCE_MASTER = "ATTENDANCE_MASTER";
    public static final String TYPE_ATTENDANCE_UPLOAD = "ATTENDANCE_UPLOAD";
    public static final String TYPE_MONTHLY_ATTENDANCE_UPLOAD = "MONTHLY_ATTENDANCE_UPLOAD";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED  = "FAILED";

    private @Autowired UploadAuditLogRepository uploadAuditLogRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(String uploadType, String fileName, String userId, String status) {
        uploadAuditLogRepository.save(UploadAuditLog.builder()
                .uploadType(uploadType)
                .fileName(fileName)
                .uploadedByUserId(userId)
                .uploadedAt(LocalDateTime.now())
                .status(status)
                .build());
    }

    public List<UploadAuditDTO> getLatestUploadsForAllTypes() {

        List<UploadAuditLog> logs = uploadAuditLogRepository.findLatestUploadsForAllTypes();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        return logs.stream().map(log -> UploadAuditDTO.builder()
                .uploadType(log.getUploadType())
                .fileName(log.getFileName())
                .uploadedByUserId(log.getUploadedByUserId())
                .uploadedAt(log.getUploadedAt().format(formatter))
                .status(log.getStatus())
                .build()
        ).toList();
    }
}
