package com.dragon.weather.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherAPIResponseDto {

    private String baseDate;
    private String baseTime;
    private String category;
    private String fcstDate;
    private String fcstTime;
    private String fcstValue;
    private Integer nx;
    private Integer ny;

    @Builder
    public WeatherAPIResponseDto(String baseDate,
                                 String baseTime,
                                 String category,
                                 String fcstDate,
                                 String fcstTime,
                                 String fcstValue,
                                 Integer nx,
                                 Integer ny) {

        this.baseDate = baseDate;
        this.baseTime = baseTime;
        this.category = category;
        this.fcstDate = fcstDate;
        this.fcstTime = fcstTime;
        this.fcstValue = fcstValue;
        this.nx = nx;
        this.ny = ny;
    }
}
