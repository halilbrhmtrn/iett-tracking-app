package com.iett.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageDTO {
    private Long id;
    private String garageName;
    private String garageCode;
    private String coordinate;
} 