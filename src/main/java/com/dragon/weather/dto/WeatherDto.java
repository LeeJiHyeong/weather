package com.dragon.weather.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherDto {

    private Double highestTemperature;
    private Double minimumTemperature;
    private Integer nx;
    private Integer ny;
    private String ptyState;
    private String date;

    @Builder
    public WeatherDto(Double highestTemperature,
                      Double minimumTemperature,
                      Integer nx,
                      Integer ny,
                      String ptyState,
                      String date) {
        this.highestTemperature = highestTemperature;
        this.minimumTemperature = minimumTemperature;
        this.nx = nx;
        this.ny = ny;
        this.ptyState = ptyState;
        this.date = date;
    }
}
