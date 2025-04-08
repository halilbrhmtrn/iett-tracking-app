package com.iett.tracking.dto.soap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoapGarageDTO {
    @JsonProperty("ID")
    private Long id;
    
    @JsonProperty("GARAJ_ADI")
    private String garageName;
    
    @JsonProperty("GARAJ_KODU")
    private String garageCode;
    
    @JsonProperty("KOORDINAT")
    private String coordinate;
} 