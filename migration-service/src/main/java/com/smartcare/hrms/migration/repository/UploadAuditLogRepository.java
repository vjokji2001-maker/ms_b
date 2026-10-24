package com.smartcare.hrms.migration.repository;

import com.smartcare.hrms.migration.enitity.audit.UploadAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadAuditLogRepository  extends JpaRepository<UploadAuditLog, Long> {

    @Query(value = """
    SELECT * 
    FROM upload_audit_log u
    WHERE u.uploaded_at = (
        SELECT MAX(u2.uploaded_at)
        FROM upload_audit_log u2
        WHERE u2.upload_type = u.upload_type
    )
    """, nativeQuery = true)
    List<UploadAuditLog> findLatestUploadsForAllTypes();
}
