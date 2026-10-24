package com.smartcare.hrms.common.repository.master.common;

import com.smartcare.hrms.common.entity.master.common.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByNameIgnoreCaseAndCountryNameIgnoreCase(String name, String countryName);

    Optional<State> findByNameIgnoreCase(String name);

}
