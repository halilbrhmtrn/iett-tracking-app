package com.iett.tracking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "buses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bus {

    @Id
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "door_no")
    private String doorNo;
    
    @Column(name = "door_number")
    private String doorNumber;
    
    @Column(name = "operator")
    private String operator;
    
    @Column(name = "garage")
    private String garage;
    
    @Column(name = "garage_code")
    private String garageCode;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "coordinate")
    private String coordinate;
    
    @Column(name = "speed")
    private Double speed;
    
    @Column(name = "license_plate")
    private String licensePlate;
    
    @Column(name = "time")
    private LocalDateTime time;
    
    @Column(name = "record_time")
    private LocalDateTime recordTime;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @Column(name = "nearest_garage_code")
    private String nearestGarageCode;
    
    @Column(name = "nearest_garage_name")
    private String nearestGarageName;
    
    @Column(name = "distance_to_nearest_garage")
    private Double distanceToNearestGarage;
} 