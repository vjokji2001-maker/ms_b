package com.smartcare.hrms.hiring.service;

import com.smartcare.hrms.hiring.converter.CandidateApplicationConverter;
import com.smartcare.hrms.hiring.dao.CandidateApplicationDAO;
import com.smartcare.hrms.hiring.dto.CandidateApplicationDTO;
import com.smartcare.hrms.hiring.dto.CandidateApplicationListDTO;
import com.smartcare.hrms.hiring.dto.CandidateApplicationResponseDTO;
import com.smartcare.hrms.hiring.model.entity.CandidateApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateApplicationService {

    private final CandidateApplicationDAO candidateApplicationDAO;
    private final CandidateResumeService candidateResumeService;
    private final CandidateApplicationConverter candidateApplicationConverter;

    public CandidateApplicationService(
            CandidateApplicationDAO candidateApplicationDAO,
            CandidateResumeService candidateResumeService,
            CandidateApplicationConverter candidateApplicationConverter) {

        this.candidateApplicationDAO = candidateApplicationDAO;
        this.candidateResumeService = candidateResumeService;
        this.candidateApplicationConverter = candidateApplicationConverter;
    }

    @Transactional
    public CandidateApplicationResponseDTO create(
            CandidateApplicationDTO dto,
            MultipartFile resume) throws IOException {

        if (resume == null || resume.isEmpty()) {
            throw new IllegalArgumentException(
                    "Resume file is required"
            );
        }

        String resumePath =
                candidateResumeService.saveResume(resume);

        CandidateApplication entity =
                candidateApplicationConverter.toEntity(dto);

        entity.setResumeFileName(
                resume.getOriginalFilename()
        );

        entity.setResumeFilePath(
                resumePath
        );

        candidateApplicationDAO.save(entity);

        return candidateApplicationConverter.toDTO(entity);
    }

    public CandidateApplicationResponseDTO getById(Long id) {

        CandidateApplication entity =
                candidateApplicationDAO.getById(id);

        if (entity == null) {
            throw new RuntimeException(
                    "Candidate application not found"
            );
        }

        return candidateApplicationConverter.toDTO(entity);
    }

    public List<CandidateApplicationListDTO> getAll() {

        List<CandidateApplication> applications =
                candidateApplicationDAO.getAll();

        return applications.stream()
                .map(candidateApplicationConverter::toListDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CandidateApplicationResponseDTO update(
            Long id,
            CandidateApplicationDTO dto,
            MultipartFile resume) throws IOException {

        CandidateApplication entity =
                candidateApplicationDAO.getById(id);

        if (entity == null) {
            throw new RuntimeException(
                    "Candidate application not found"
            );
        }

        entity.setCandidateName(
                dto.getCandidateName()
        );

        entity.setMobileNumber(
                dto.getMobileNumber()
        );

        entity.setEmail(
                dto.getEmail()
        );

        entity.setGender(
                dto.getGender()
        );

        entity.setDateOfBirth(
                dto.getDateOfBirth()
        );

        entity.setExperience(
                dto.getExperience()
        );

        entity.setCurrentCompany(
                dto.getCurrentCompany()
        );

        entity.setCurrentCtc(
                dto.getCurrentCtc()
        );

        entity.setExpectedCtc(
                dto.getExpectedCtc()
        );

        entity.setNoticePeriod(
                dto.getNoticePeriod()
        );

        entity.setAddress(
                dto.getAddress()
        );

        entity.setRemarks(
                dto.getRemarks()
        );

        if (resume != null && !resume.isEmpty()) {

            String oldResumePath =
                    entity.getResumeFilePath();

            String newResumePath =
                    candidateResumeService.saveResume(
                            resume
                    );

            entity.setResumeFileName(
                    resume.getOriginalFilename()
            );

            entity.setResumeFilePath(
                    newResumePath
            );

            candidateResumeService.deleteResume(
                    oldResumePath
            );
        }

        candidateApplicationDAO.update(entity);

        return candidateApplicationConverter.toDTO(entity);
    }

    @Transactional
    public void delete(Long id) {

        CandidateApplication entity =
                candidateApplicationDAO.getById(id);

        if (entity == null) {
            throw new RuntimeException(
                    "Candidate application not found"
            );
        }

        candidateResumeService.deleteResume(
                entity.getResumeFilePath()
        );

        candidateApplicationDAO.delete(entity);
    }
}