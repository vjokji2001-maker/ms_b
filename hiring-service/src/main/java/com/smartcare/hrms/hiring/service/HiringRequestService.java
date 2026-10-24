package com.smartcare.hrms.hiring.service;


import org.springframework.stereotype.Service;
import com.smartcare.hrms.hiring.dto.CareerJobDTO;
import com.smartcare.hrms.hiring.dto.HiringRequestDTO;
import com.smartcare.hrms.hiring.dto.HiringStatusResponse;
import com.smartcare.hrms.hiring.helper.HiringMessageSource;
import com.smartcare.hrms.hiring.model.entity.HiringRequest;
import org.springframework.transaction.annotation.Transactional;
import com.smartcare.hrms.hiring.model.entity.enums.RequestStatus;
import com.smartcare.hrms.hiring.converter.HiringRequestConverter;
import com.smartcare.hrms.hiring.repository.HiringRequestRepository;

import java.util.List;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@Transactional
public class HiringRequestService {

    private final HiringRequestRepository hiringRequestRepository;
    private final HiringRequestConverter hiringRequestConverter;
    private final HiringMessageSource hiringMessageSource;

    public HiringRequestService(
            HiringRequestRepository hiringRequestRepository,
            HiringRequestConverter hiringRequestConverter,
            HiringMessageSource hiringMessageSource) {
        this.hiringRequestRepository = hiringRequestRepository;
        this.hiringRequestConverter = hiringRequestConverter;
        this.hiringMessageSource = hiringMessageSource;
    }

    public HiringRequestDTO create(HiringRequestDTO dto) {

        HiringRequest hiringRequest = hiringRequestConverter.toEntity(dto);

        HiringRequest saved = hiringRequestRepository.save(hiringRequest);

        saved.setRequestId("HR/" + LocalDate.now().getYear() + "/" +
                        String.format("%03d", saved.getId()));

        saved = hiringRequestRepository.save(saved);

        return hiringRequestConverter.toDTO(saved);
    }

    public List<HiringRequestDTO> getAll() {
        return hiringRequestRepository.findAll()
                .stream()
                .map(hiringRequestConverter::toDTO)
                .collect(Collectors.toList());
    }

    public HiringRequestDTO getById(Long id) {
        HiringRequest entity = hiringRequestRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(hiringMessageSource.getMessage("hiring.request.not.found")));

        return hiringRequestConverter.toDTO(entity);
    }

    public HiringStatusResponse approve(Long id) {

        HiringRequest hiringRequest =hiringRequestRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(hiringMessageSource.getMessage("hiring.request.not.found")));

        hiringRequest.setStatus(RequestStatus.APPROVED);
        hiringRequestRepository.save(hiringRequest);

        return new HiringStatusResponse(
                id,
                hiringMessageSource.getMessage("hiring.request.approved"),
                RequestStatus.APPROVED
        );
    }

    public HiringStatusResponse reject(Long id) {

        HiringRequest hiringRequest = hiringRequestRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(hiringMessageSource.getMessage("hiring.request.not.found")));

        hiringRequest.setStatus(RequestStatus.REJECTED);
        hiringRequestRepository.save(hiringRequest);

        return new HiringStatusResponse(
                id,
                hiringMessageSource.getMessage("hiring.request.rejected"),
                RequestStatus.REJECTED
        );
    }

    public List<CareerJobDTO> getApprovedJobs() {
        return hiringRequestRepository.findByStatus(RequestStatus.APPROVED)
                .stream()
                .map(hiringRequestConverter::toCareerDTO)
                .collect(Collectors.toList());
    }

}