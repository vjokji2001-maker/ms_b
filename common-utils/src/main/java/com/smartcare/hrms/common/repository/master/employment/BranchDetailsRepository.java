package com.smartcare.hrms.common.repository.master.employment;

import java.util.List;
import java.util.Optional;

import com.smartcare.hrms.common.entity.master.employment.CompanyDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.bean.master.employment.BranchDetailsBean;
import com.smartcare.hrms.common.entity.master.employment.BranchDetails;

@Repository
public interface BranchDetailsRepository extends JpaRepository<BranchDetails, Long> {
    @Query(value = "SELECT SQL_NO_CACHE bd.id, bd.name, bd.code, bd.address, cm.id countryId, cm.name countryName, sm.id stateId, sm.name stateName, ct.id cityId, ct.name cityName FROM branch_details bd " +
            "INNER JOIN country_master cm ON cm.id = bd.country_id " +
            "INNER JOIN state_master sm ON sm.id = bd.state_id " +
            "INNER JOIN city_master ct ON ct.id = bd.city_id ", nativeQuery=true)
    List<BranchDetailsBean> getAll();

    @Query(value = "SELECT SQL_NO_CACHE bd.id, bd.name, bd.code, bd.address, cm.id countryId, cm.name countryName, sm.id stateId, sm.name stateName, ct.id cityId, ct.name cityName FROM branch_details bd " +
            "INNER JOIN country_master cm ON cm.id = bd.country_id " +
            "INNER JOIN state_master sm ON sm.id = bd.state_id " +
            "INNER JOIN city_master ct ON ct.id = bd.city_id " +
            "WHERE bd.id = :id ", nativeQuery=true)
    BranchDetailsBean getAllById(@Param("id") Long id);

    @Query(value = "SELECT SQL_NO_CACHE bd.id, bd.name, bd.code, bd.address, cm.id countryId, cm.name countryName, sm.id stateId, sm.name stateName, ct.id cityId, ct.name cityName FROM branch_details bd " +
            "INNER JOIN country_master cm ON cm.id = bd.country_id " +
            "INNER JOIN state_master sm ON sm.id = bd.state_id " +
            "INNER JOIN city_master ct ON ct.id = bd.city_id " +
            "WHERE bd.name LIKE (%:searchText%)", nativeQuery=true)
    List<BranchDetailsBean> getByName(@Param("searchText") String searchText);

    @Query(value = "SELECT SQL_NO_CACHE bd.id, bd.name, bd.code, bd.address, cm.id countryId, cm.name countryName, sm.id stateId, sm.name stateName, ct.id cityId, ct.name cityName FROM branch_details bd " +
            "INNER JOIN country_master cm ON cm.id = bd.country_id " +
            "INNER JOIN state_master sm ON sm.id = bd.state_id " +
            "INNER JOIN city_master ct ON ct.id = bd.city_id " +
            "WHERE bd.code = :searchText ", nativeQuery=true)
    BranchDetailsBean getByCode(@Param("searchText") String searchText);

    BranchDetails findByCode(String code);

    boolean existsByCode(String code);

    Optional<BranchDetails> findByCodeIgnoreCase(String code);

}
