package com.smartcare.hrms.staff.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Component
public class FaceValidationHelper {

    @Value("${file.upload.dir:/staging_employee_uploads}")
    private String baseUploadDir;

    @Value("${server.base.url:http://103.177.84.241:9099/hr/staff/}")
    private String serverBaseUrl;

    /**
     * Validates that base64 image data is present and well-formed.
     */
    public boolean isValidFaceImage(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return false;
        }
        try {
            String cleanBase64 = stripBase64Header(base64Image);
            byte[] decoded = Base64.getDecoder().decode(cleanBase64);
            // Minimum size check (e.g. at least 1 KB) to ensure real image
            return decoded.length > 1024;
        } catch (Exception e) {
            log.error("Failed to decode base64 face image", e);
            return false;
        }
    }

    /**
     * Saves the selfie / punch photo on disk under the tenant's upload directory.
     *
     * @param base64Image base64 encoded photo
     * @param clinicId tenant/clinic ID
     * @param empCode employee code
     * @param punchType IN / OUT
     * @return file path / relative URL
     */
    public String savePunchSelfie(String base64Image, String clinicId, String empCode, String punchType) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return null;
        }

        try {
            String cleanBase64 = stripBase64Header(base64Image);
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

            String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Path dirPath = Paths.get(baseUploadDir, clinicId != null ? clinicId : "default", "attendance", "punches", todayStr);

            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String filename = empCode + "_" + punchType + "_" + System.currentTimeMillis() + ".jpg";
            Path filePath = dirPath.resolve(filename);

            try (OutputStream os = new FileOutputStream(filePath.toFile())) {
                os.write(imageBytes);
            }

            log.info("Saved punch selfie to: {}", filePath.toAbsolutePath());
            return filePath.toAbsolutePath().toString();
        } catch (Exception e) {
            log.error("Error saving punch selfie", e);
            return null;
        }
    }

    private String stripBase64Header(String base64) {
        if (base64.contains(",")) {
            return base64.split(",")[1].trim();
        }
        return base64.trim();
    }
}
