package com.smartcare.hrms.master.service.common;

import com.smartcare.hrms.common.bean.master.location.LocationDetailBean;
import com.smartcare.hrms.common.dto.master.common.CityDTO;
import com.smartcare.hrms.common.repository.master.common.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private @Autowired CountryRepository countryRepository;



    public CityDTO.LocationResponseDTO getAll() {
        List<LocationDetailBean> beans = countryRepository.getCountryStateCityNameList();

        List<CityDTO.CountryDTO> countries = beans.stream()
                .map(b -> CityDTO.CountryDTO.builder()
                        .countryId(b.getCountryId())
                        .countryName(b.getCountryName())
                        .build())
                .distinct()
                .collect(Collectors.toList());

        List<CityDTO.StateDTO> states = beans.stream()
                .map(b -> CityDTO.StateDTO.builder()
                        .stateId(b.getStateId())
                        .stateName(b.getStateName())
                        .countryId(b.getCountryId())
                        .build())
                .distinct()
                .collect(Collectors.toList());

        List<CityDTO> cities = beans.stream()
                .map(b -> CityDTO.builder()
                        .cityId(b.getCityId())
                        .cityName(b.getCityName())
                        .stateId(b.getStateId())
                        .build())
                .distinct()
                .collect(Collectors.toList());

        return CityDTO.LocationResponseDTO.builder()
                .countries(countries)
                .states(states)
                .cities(cities)
                .build();
    }
}
