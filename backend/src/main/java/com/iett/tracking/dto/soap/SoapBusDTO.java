package com.iett.tracking.dto.soap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoapBusDTO {
    @JsonProperty("Operator")
    private String operator;
    
    @JsonProperty("Garaj")
    private String garage;
    
    @JsonProperty("KapiNo")
    private String doorNo;
    
    @JsonProperty("Saat")
    private String time;
    
    @JsonProperty("Boylam")
    private String longitude;
    
    @JsonProperty("Enlem")
    private String latitude;
    
    @JsonProperty("Hiz")
    private String speed;
    
    @JsonProperty("Plaka")
    private String licensePlate;
} 