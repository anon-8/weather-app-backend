package com.codibly.greenEnergyCalc.weather;

import com.codibly.greenEnergyCalc.weather.domain.ApiResponse;
import com.codibly.greenEnergyCalc.weather.domain.WeatherDTO;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class WeatherService {

    private static RestTemplate restTemplate;
    private static String weatherAPIEndpoint;

    @Value("${weatherAPI.endpoint}")
    public void setWeatherAPIEndpoint(String endpoint) {
        weatherAPIEndpoint = endpoint;
    }

    public static WeatherDTO getWeatherData(Map<String, String> parameters) {

        validateParameters(parameters);

        initializeRestTemplate();

        ResponseEntity<String> response = restTemplate.getForEntity(getWeatherApiUri(parameters), String.class);

        ApiResponse gsonResponse = new Gson().fromJson(response.getBody(), ApiResponse.class);

        Map<String, Map<String, Map<String, Object>>> processedHourlyDataMap = processHourlyData(gsonResponse.getHourly());

        Map<String, Map<String, String>> processedDailyDataMap = processDailyData(gsonResponse.getDaily());

        Map<String, Object> requestData = createRequestData(gsonResponse);

        return WeatherDTO.builder()
                .requestData(requestData)
                .hourlyWeatherData(processedHourlyDataMap)
                .dailyWeatherData(processedDailyDataMap)
                .build();
    }

    private static void validateParameters(Map<String, String> parameters) {
        String latitude = parameters.get("latitude");
        String longitude = parameters.get("longitude");

        if (!parameters.containsKey("latitude") || !parameters.containsKey("longitude")) {
            throw new IllegalArgumentException("Latitude and longitude parameters are required.");
        }
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude and longitude must be provided.");
        }
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90, and longitude must be between -180 and 180.");
        }
    }

    private static void initializeRestTemplate() {
        if (restTemplate == null) restTemplate = new RestTemplate();
    }

    private static Map<String, Object> createRequestData(ApiResponse apiResponse) {
        Map<String, Object> requestData = new LinkedHashMap<>();
        Map<String, String> parametersMap = new LinkedHashMap<>();

        Field[] fields = ApiResponse.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(apiResponse);
                if (fieldValue instanceof String) {
                    parametersMap.put(field.getName(), (String) fieldValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing field: {}", e);
            }
        }

        requestData.put("parameters", parametersMap);

        requestData.put("hourly_units", apiResponse.getHourly_units());
        requestData.put("daily_units", apiResponse.getDaily_units());

        return requestData;
    }

    private static Map<String, Map<String, Map<String, Object>>> processHourlyData(Map<String, List<String>> hourlyData) {

        Set<String> fieldNames = hourlyData.keySet();

        validateHourly(hourlyData);

        Map<String, Map<String, Map<String, Object>>> processedDataMap = new LinkedHashMap<>();
        for (int i = 0; i < hourlyData.get("time").size(); i++) {

            LocalDateTime dateTime = LocalDateTime.parse(hourlyData.get("time").get(i));

            String date = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            String hour = dateTime.format(DateTimeFormatter.ofPattern("HH"));

            Map<String, Object> currentHourData = new LinkedHashMap<>();
            for (String fieldName : fieldNames) {

                String value = hourlyData.get(fieldName).get(i);

                currentHourData.put(fieldName, value);
            }

            processedDataMap.computeIfAbsent(date, k -> new LinkedHashMap<>());

            Map<String, Map<String, Object>> currentDayStats = processedDataMap.get(date);
            currentDayStats.computeIfAbsent("hours", k -> new LinkedHashMap<>());

            currentDayStats.get("hours").put(hour, currentHourData);
        }

        return processedDataMap;
    }

    private static void validateHourly(Map<String, List<String>> hourlyData) {
        if (!hourlyData.containsKey("time") || !hourlyData.containsKey("temperature_2m")) {
            throw new IllegalArgumentException("Hourly data must contain time and temperature_2m parameters.");
        }
    }

    private static Map<String, Map<String, String>> processDailyData(Map<String, List<String>> dailyData) {

        Set<String> fieldNames = dailyData.keySet();

        validateDaily(dailyData);

        Map<String, Map<String, String>> processedDataMap = new LinkedHashMap<>();
        for (int i=0; i<dailyData.get("time").size(); i++) {

            LocalDate date = LocalDate.parse(dailyData.get("time").get(i));

            String formatedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            Map<String, String> currentDayData = new LinkedHashMap<>();

            for (String fieldName : fieldNames) {
                String value = dailyData.get(fieldName).get(i);
                currentDayData.put(fieldName, value);

                if (fieldName.equals("sunshine_duration")) {
                    DecimalFormat df = new DecimalFormat("#.##");
                    String estimatedPowerGenerated = df.format(Double.parseDouble(value)/60/60 * 0.2 * 2.5);
                    currentDayData.put("estimatedPowerGenerated", estimatedPowerGenerated);
                }
            }

            processedDataMap.put(formatedDate, currentDayData);

        }

        return processedDataMap;
    }

    private static void validateDaily(Map<String, List<String>> dailyData) {
        if (!dailyData.containsKey("time") || !dailyData.containsKey("weather_code") || !dailyData.containsKey("temperature_2m_max")
                || !dailyData.containsKey("temperature_2m_min") || !dailyData.containsKey("sunshine_duration")) {
            throw new IllegalArgumentException("Daily data must contain time, weather_code, temperature_2m_max, temperature_2m_min, and sunshine_duration parameters.");
        }
    }

    protected static String getWeatherApiUri(Map<String, String> parameters){
        StringJoiner queryString = new StringJoiner("&");
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            queryString.add(entry.getKey() + "=" + entry.getValue());
        }

        return weatherAPIEndpoint + "?" + queryString;
    }

}
