package com.smartcare.hrms.common.repository.master.employment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.bean.master.employment.CompanyDetailsBean;
import com.smartcare.hrms.common.entity.master.employment.CompanyDetails;

@Repository
public interface CompanyDetailsRepository extends JpaRepository<CompanyDetails, Long> {

	@Query(value = "SELECT SQL_NO_CACHE cd.id AS id, cd.name AS name, cd.code AS code, cd.reg_name AS registeredName, cd.reg_no AS registeredNumber, "
            + "cd.inc_dt AS incorporationDate, cd.pan_no AS panNumber, cd.tan_no AS tanNumber, cd.cin_no AS cinNumber, cd.gstin AS gstin, cd.pt_reg_no AS ptRegNumber, "
            + "cd.pf_reg_no AS pfRegistrationNumber, cd.esic_reg_no AS esicRegNumber, cd.msme_reg_no AS msmeRegNo, cd.iec_code AS iecCode, cd.pt_zone AS ptZone, "
			+ "cd.pf_zone AS pfZone, cd.esic_zone AS esicZone, cd.Industry_type AS industryType, cd.address AS address, cm.id AS countryId, cm.name AS countryName,  "
			+ "sm.id AS stateId, sm.name AS stateName, ct.id AS cityId, ct.name AS cityName, cd.pin_code AS pincode, cd.contact_prsn_name AS contactPersonName, "
            + "d.id AS designationId, d.name AS designationName, d.code AS designationCode, cd.contact_prsn_email AS contactPersonEmail, cd.country_code AS countryCode, cd.phone AS phone, "
			+ "cd.email AS email, cd.website AS website, cd.twitter AS twitter, cd.facebook AS facebook, cd.instagram AS instagram, cd.image_url AS imageUrl "
            + "FROM company_details AS cd "
            + "INNER JOIN country_master AS cm ON cm.id = cd.country_id "
            + "INNER JOIN state_master AS sm ON sm.id = cd.state_id "
			+ "INNER JOIN city_master AS ct ON ct.id = cd.city_id "
			+ "INNER JOIN designation AS d ON d.id = cd.desig_id ", nativeQuery = true)
	List<CompanyDetailsBean> getAll();

    @Query(value = "SELECT SQL_NO_CACHE cd.id AS id, cd.name AS name, cd.code AS code, cd.reg_name AS registeredName, cd.reg_no AS registeredNumber, "
            + "cd.inc_dt AS incorporationDate, cd.pan_no AS panNumber, cd.tan_no AS tanNumber, cd.cin_no AS cinNumber, cd.gstin AS gstin, cd.pt_reg_no AS ptRegNumber, "
            + "cd.pf_reg_no AS pfRegistrationNumber, cd.esic_reg_no AS esicRegNumber, cd.msme_reg_no AS msmeRegNo, cd.iec_code AS iecCode, cd.pt_zone AS ptZone, "
            + "cd.pf_zone AS pfZone, cd.esic_zone AS esicZone, cd.Industry_type AS industryType, cd.address AS address, cm.id AS countryId, cm.name AS countryName,  "
            + "sm.id AS stateId, sm.name AS stateName, ct.id AS cityId, ct.name AS cityName, cd.pin_code AS pincode, cd.contact_prsn_name AS contactPersonName, "
            + "d.id AS designationId, d.name AS designationName, d.code AS designationCode, cd.contact_prsn_email AS contactPersonEmail, cd.country_code AS countryCode, cd.phone AS phone, "
            + "cd.email AS email, cd.website AS website, cd.twitter AS twitter, cd.facebook AS facebook, cd.instagram AS instagram, cd.image_url AS imageUrl "
            + "FROM company_details AS cd "
            + "INNER JOIN country_master AS cm ON cm.id = cd.country_id "
            + "INNER JOIN state_master AS sm ON sm.id = cd.state_id "
            + "INNER JOIN city_master AS ct ON ct.id = cd.city_id "
            + "INNER JOIN designation AS d ON d.id = cd.desig_id "
			+ "WHERE cd.name LIKE (%:searchText%)" , nativeQuery = true)
	List<CompanyDetailsBean> getByName(@Param("searchText") String searchText);


    @Query(value = "SELECT SQL_NO_CACHE cd.id AS id, cd.name AS name, cd.code AS code, cd.reg_name AS registeredName, cd.reg_no AS registeredNumber, "
            + "cd.inc_dt AS incorporationDate, cd.pan_no AS panNumber, cd.tan_no AS tanNumber, cd.cin_no AS cinNumber, cd.gstin AS gstin, cd.pt_reg_no AS ptRegNumber, "
            + "cd.pf_reg_no AS pfRegistrationNumber, cd.esic_reg_no AS esicRegNumber, cd.msme_reg_no AS msmeRegNo, cd.iec_code AS iecCode, cd.pt_zone AS ptZone, "
            + "cd.pf_zone AS pfZone, cd.esic_zone AS esicZone, cd.Industry_type AS industryType, cd.address AS address, cm.id AS countryId, cm.name AS countryName,  "
            + "sm.id AS stateId, sm.name AS stateName, ct.id AS cityId, ct.name AS cityName, cd.pin_code AS pincode, cd.contact_prsn_name AS contactPersonName, "
            + "d.id AS designationId, d.name AS designationName, d.code AS designationCode, cd.contact_prsn_email AS contactPersonEmail, cd.country_code AS countryCode, cd.phone AS phone, "
            + "cd.email AS email, cd.website AS website, cd.twitter AS twitter, cd.facebook AS facebook, cd.instagram AS instagram, cd.image_url AS imageUrl "
            + "FROM company_details AS cd "
            + "INNER JOIN country_master AS cm ON cm.id = cd.country_id "
            + "INNER JOIN state_master AS sm ON sm.id = cd.state_id "
            + "INNER JOIN city_master AS ct ON ct.id = cd.city_id "
            + "INNER JOIN designation AS d ON d.id = cd.desig_id "
            + "WHERE cd.code = :searchText" , nativeQuery = true)
    CompanyDetailsBean getByCode(@Param("searchText") String searchText);


    @Query(value = "SELECT SQL_NO_CACHE cd.id AS id, cd.name AS name, cd.code AS code, cd.reg_name AS registeredName, cd.reg_no AS registeredNumber, "
            + "cd.inc_dt AS incorporationDate, cd.pan_no AS panNumber, cd.tan_no AS tanNumber, cd.cin_no AS cinNumber, cd.gstin AS gstin, cd.pt_reg_no AS ptRegNumber, "
            + "cd.pf_reg_no AS pfRegistrationNumber, cd.esic_reg_no AS esicRegNumber, cd.msme_reg_no AS msmeRegNo, cd.iec_code AS iecCode, cd.pt_zone AS ptZone, "
            + "cd.pf_zone AS pfZone, cd.esic_zone AS esicZone, cd.Industry_type AS industryType, cd.address AS address, cm.id AS countryId, cm.name AS countryName,  "
            + "sm.id AS stateId, sm.name AS stateName, ct.id AS cityId, ct.name AS cityName, cd.pin_code AS pincode, cd.contact_prsn_name AS contactPersonName, "
            + "d.id AS designationId, d.name AS designationName, d.code AS designationCode, cd.contact_prsn_email AS contactPersonEmail, cd.country_code AS countryCode, cd.phone AS phone, "
            + "cd.email AS email, cd.website AS website, cd.twitter AS twitter, cd.facebook AS facebook, cd.instagram AS instagram, cd.image_url AS imageUrl  "
            + "FROM company_details AS cd "
            + "INNER JOIN country_master AS cm ON cm.id = cd.country_id "
            + "INNER JOIN state_master AS sm ON sm.id = cd.state_id "
            + "INNER JOIN city_master AS ct ON ct.id = cd.city_id "
            + "INNER JOIN designation AS d ON d.id = cd.desig_id "
			+ "WHERE cd.id = :id " , nativeQuery = true)
	CompanyDetailsBean getAllById(@Param("id") Long id);


    CompanyDetails findByCode(String code);
	CompanyDetails findByRegisteredName(String registeredName);
	CompanyDetails findByRegisteredNumber(String registeredNumber);
	CompanyDetails findByPanNumber(String panNumber);
	CompanyDetails findByTanNumber(String tanNumber);
	CompanyDetails findByCinNumber(String cinNumber);
	CompanyDetails findByGstin(String gstin);
	CompanyDetails findByPtRegNumber(String ptRegNumber);
	CompanyDetails findByPfRegistrationNumber(String pfRegistrationNumber);
	CompanyDetails findByEsicRegNumber(String esicRegNumber);
	CompanyDetails findByMsmeRegNo(String msmeRegNo);
	CompanyDetails findByIecCode(String iecCode);

    Boolean existsByCode(String code);

    Optional<CompanyDetails> findByCodeIgnoreCase(String code);
}
