package com.smartcare.hrms.hiring.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class CandidateResumeService {

    @Value("${candidate.resume.upload-dir}")
    private String uploadDirectory;

    public String saveResume(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Resume file is required"
            );
        }

        String originalFileName =
                file.getOriginalFilename();

        if (originalFileName == null ||
                originalFileName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Invalid resume file"
            );
        }

        if (!originalFileName
                .toLowerCase()
                .endsWith(".pdf")) {

            throw new IllegalArgumentException(
                    "Only PDF resume files are allowed"
            );
        }

        Path uploadPath =
                Paths.get(uploadDirectory)
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(uploadPath);

        String fileName =
                UUID.randomUUID()
                        .toString()
                        + "_"
                        + originalFileName;

        Path filePath =
                uploadPath.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return filePath.toString();
    }

    public void deleteResume(String filePath) {

        if (filePath == null ||
                filePath.trim().isEmpty()) {
            return;
        }

        try {
            Path path = Paths.get(filePath);

            Files.deleteIfExists(path);

        } catch (Exception ignored) {
        }
    }
}