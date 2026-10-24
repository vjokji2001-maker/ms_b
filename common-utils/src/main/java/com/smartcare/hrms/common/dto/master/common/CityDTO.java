package com.smartcare.hrms.common.dto.master.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CityDTO {
    private Long cityId;
    private String cityName;
    private Long stateId;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CountryDTO {
        private Long countryId;
        private String countryName;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationResponseDTO {

        private List<CountryDTO> countries;
        private List<StateDTO> states;
        private List<CityDTO> cities;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StateDTO {
        private Long stateId;
        private String stateName;
        private Long countryId;
    }
}
