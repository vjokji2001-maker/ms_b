package com.smartcare.hrms.common.repository.master.common;

import com.smartcare.hrms.common.entity.master.common.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByNameIgnoreCaseAndStateNameIgnoreCase(String name, String stateName);

    Optional<City> findByNameIgnoreCase(String name);

}
