package com.dragon.weather.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private Double highestTemperature;
    private Double minimumTemperature;
    private Integer nx;
    private Integer ny;
    private String ptyState;
    private String date;

    @Builder
    public Weather(Double highestTemperature,
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
