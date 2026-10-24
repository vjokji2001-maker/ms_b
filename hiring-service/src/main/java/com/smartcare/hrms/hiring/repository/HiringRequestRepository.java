package com.smartcare.hrms.hiring.repository;

import org.springframework.stereotype.Repository;
import com.smartcare.hrms.hiring.model.entity.HiringRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartcare.hrms.hiring.model.entity.enums.RequestStatus;

import java.util.List;

@Repository
public interface HiringRequestRepository extends JpaRepository<HiringRequest, Long> {

    List<HiringRequest> findByStatus(RequestStatus status);

}