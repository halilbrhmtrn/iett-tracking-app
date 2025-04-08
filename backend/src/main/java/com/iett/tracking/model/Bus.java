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
    @Column(name = "door_no")
    private String doorNo;
    
    @Column(name = "operator")
    private String operator;
    
    @Column(name = "garage")
    private String garage;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "speed")
    private Double speed;
    
    @Column(name = "license_plate")
    private String licensePlate;
    
    @Column(name = "time")
    private LocalDateTime time;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
} 