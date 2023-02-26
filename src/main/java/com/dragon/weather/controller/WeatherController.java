package com.dragon.weather.controller;

import com.dragon.weather.dto.WeatherDto;
import com.dragon.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@Slf4j
@RequiredArgsConstructor
@RestController
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping(value = "/today")
    public WeatherDto todayWeather() throws UnsupportedEncodingException {

        log.info("today controller");
        WeatherDto weatherDto = this.weatherService.get();
        return weatherDto;
    }
}
