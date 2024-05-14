package com.codibly.greenEnergyCalc.weather;

import com.codibly.greenEnergyCalc.weather.domain.WeatherDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void testGetWeatherData() {

        WeatherService weatherService = new WeatherService();

        weatherService.setWeatherAPIEndpoint("https://api.open-meteo.com/v1/forecast");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("latitude", "52.52");
        params.put("longitude", "-77.18");
        params.put("hourly", "temperature_2m,sunshine_duration");
        params.put("daily", "weather_code,temperature_2m_max,temperature_2m_min,sunshine_duration");

        WeatherDTO result = WeatherService.getWeatherData(params);

        assertFalse(result.getRequestData().isEmpty());
        assertFalse(result.getHourlyWeatherData().isEmpty());
        assertFalse(result.getDailyWeatherData().isEmpty());
    }
}
