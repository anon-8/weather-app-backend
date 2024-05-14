package com.codibly.greenEnergyCalc.weather;

import com.codibly.greenEnergyCalc.weather.domain.WeatherDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WeatherController {

    @GetMapping("/getWeatherData")
    public ResponseEntity<WeatherDTO> getWeatherData(@RequestParam Map<String, String> parameters) {
        WeatherDTO weatherDTO = WeatherService.getWeatherData(parameters);

        return new ResponseEntity<>(weatherDTO, HttpStatus.OK);
    }
}