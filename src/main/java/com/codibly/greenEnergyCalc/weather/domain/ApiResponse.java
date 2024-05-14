package com.codibly.greenEnergyCalc.weather.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ApiResponse {

    private String latitude;
    private String longitude;
    private String generationtime_ms;
    private String utc_offset_seconds;
    private String timezone;
    private String timezone_abbreviation;
    private String elevation;

    private Map<String, List<String>> hourly;
    private Map<String, String> hourly_units;

    private Map<String, List<String>> daily;
    private Map<String, String> daily_units;
}