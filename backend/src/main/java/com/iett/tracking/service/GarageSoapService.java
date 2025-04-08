package com.iett.tracking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iett.tracking.dto.soap.SoapGarageDTO;
import com.iett.tracking.model.DataRetrievalLog;
import com.iett.tracking.model.Garage;
import com.iett.tracking.repository.DataRetrievalLogRepository;
import com.iett.tracking.repository.GarageRepository;
import com.iett.tracking.util.SoapUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GarageSoapService {
    
    private final RestTemplate restTemplate;
    private final GarageRepository garageRepository;
    private final DataRetrievalLogRepository dataRetrievalLogRepository;
    private final SoapUtils soapUtils;
    
    @Value("${iett.soap.garage-service-url}")
    private String serviceUrl;
    
    @Value("${iett.soap.garage-method}")
    private String methodName;
    
    @Value("${iett.soap.data-cache-duration-minutes}")
    private int cacheDurationMinutes;
    
    public GarageSoapService(
            RestTemplate restTemplate,
            GarageRepository garageRepository,
            DataRetrievalLogRepository dataRetrievalLogRepository,
            SoapUtils soapUtils) {
        this.restTemplate = restTemplate;
        this.garageRepository = garageRepository;
        this.dataRetrievalLogRepository = dataRetrievalLogRepository;
        this.soapUtils = soapUtils;
    }
    
    public List<Garage> getGarageData() {
        boolean needsRefresh = needsDataRefresh();
        
        if (!needsRefresh) {
            log.info("Using cached garage data from database");
            return garageRepository.findAll();
        }
        
        log.info("Fetching fresh garage data from SOAP service");
        List<SoapGarageDTO> garageData = fetchGarageDataFromSoap();
        
        if (!garageData.isEmpty()) {
            updateGarageDatabase(garageData);
            return garageRepository.findAll();
        }
        
        return garageRepository.findAll();
    }
    

    private boolean needsDataRefresh() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(cacheDurationMinutes);
        
        return !dataRetrievalLogRepository.existsRecentSuccessfulRetrieval(
                DataRetrievalLog.DataType.GARAGE, threshold);
    }
    
    private List<SoapGarageDTO> fetchGarageDataFromSoap() {
        try {
            String soapRequest = 
                    "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>" +
                    "<soap:Body>" +
                    "<" + methodName + " xmlns=\"http://tempuri.org/\" />" +
                    "</soap:Body>" +
                    "</soap:Envelope>";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", "http://tempuri.org/" + methodName);
            
            HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);
            
            log.info("Sending SOAP request to {}", serviceUrl);
            
            String responseXml = restTemplate.postForObject(serviceUrl, request, String.class);
            
            JSONObject jsonObj = XML.toJSONObject(responseXml);
            
            JSONObject envelope = jsonObj.getJSONObject("soap:Envelope");
            JSONObject body = envelope.getJSONObject("soap:Body");
            JSONObject response = body.getJSONObject(methodName + "Response");
            String jsonData = response.getString(methodName + "Result");
            
            logDataRetrieval(true, null);
            
            return soapUtils.parseJsonToList(jsonData, new TypeReference<List<SoapGarageDTO>>() {});
        } catch (Exception e) {
            log.error("Error fetching garage data: {}", e.getMessage(), e);
            logDataRetrieval(false, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private void updateGarageDatabase(List<SoapGarageDTO> garageDTOs) {
        garageRepository.deleteAll();
        
        List<Garage> garages = garageDTOs.stream()
                .map(this::mapToGarageEntity)
                .collect(Collectors.toList());
        
        garageRepository.saveAll(garages);
        log.info("Saved {} garages to database", garages.size());
    }
    
    private Garage mapToGarageEntity(SoapGarageDTO dto) {
        Garage garage = new Garage();
        garage.setId(dto.getId());
        garage.setGarageName(dto.getGarageName());
        garage.setGarageCode(dto.getGarageCode());
        
        String coordinate = soapUtils.parsePointToCoordinate(dto.getCoordinate());
        garage.setCoordinate(coordinate);
        
        garage.setLastUpdated(LocalDateTime.now());
        return garage;
    }
    
    private void logDataRetrieval(boolean success, String errorMessage) {
        DataRetrievalLog log = new DataRetrievalLog();
        log.setDataType(DataRetrievalLog.DataType.GARAGE);
        log.setRetrievalTime(LocalDateTime.now());
        log.setSuccess(success);
        log.setErrorMessage(errorMessage);
        
        dataRetrievalLogRepository.save(log);
    }
} 