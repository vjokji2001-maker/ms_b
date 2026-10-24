package com.smartcare.hrms.migration.enitity.audit;

import com.smartcare.hrms.common.entity.BaseEntityId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "upload_audit_log")
public class UploadAuditLog extends BaseEntityId {

    @Column(name = "upload_type", nullable = false)
    private String uploadType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedByUserId;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "status", nullable = false)
    private String status;
}
