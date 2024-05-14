package com.codibly.greenEnergyCalc.weather.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class WeatherDTO {

    private Map<String, Object> requestData;

    private Map<String, Map<String, Map<String, Object>>> hourlyWeatherData;

    private Map<String, Map<String, String>> dailyWeatherData;

}
