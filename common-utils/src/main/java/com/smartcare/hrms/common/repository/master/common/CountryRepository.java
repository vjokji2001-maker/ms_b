package com.smartcare.hrms.common.repository.master.common;


import com.smartcare.hrms.common.bean.master.location.LocationDetailBean;
import com.smartcare.hrms.common.entity.master.common.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query(value="SELECT c.id AS cityId, c.name AS cityName, s.id AS stateId,s.name AS stateName, co.id AS countryId,co.name AS countryName " +
            "FROM city_master c " +
            " INNER JOIN state_master s ON c.state_id = s.id " +
            "INNER JOIN country_master co ON s.country_id = co.id " +
            "ORDER BY co.name ASC ,s.name ASC,c.name ASC ", nativeQuery = true)
    List<LocationDetailBean> getCountryStateCityNameList();

    Optional<Country> findByNameIgnoreCase(String name);

}
