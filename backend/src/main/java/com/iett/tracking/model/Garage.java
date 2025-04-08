package com.iett.tracking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "garages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "garage_name", nullable = false)
    private String garageName;
    
    @Column(name = "garage_code", nullable = false)
    private String garageCode;
    
    @Column(name = "coordinate", columnDefinition = "TEXT")
    private String coordinate;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
} 