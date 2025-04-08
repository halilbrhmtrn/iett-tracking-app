package com.iett.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusDTO {
    private String doorNo;
    private String operator;
    private String garage;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private String licensePlate;
    private LocalDateTime time;
} 