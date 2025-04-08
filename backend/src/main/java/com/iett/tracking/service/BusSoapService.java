package com.iett.tracking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iett.tracking.dto.soap.SoapBusDTO;
import com.iett.tracking.model.Bus;
import com.iett.tracking.model.DataRetrievalLog;
import com.iett.tracking.model.Garage;
import com.iett.tracking.repository.BusRepository;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BusSoapService {
    
    private final RestTemplate restTemplate;
    private final BusRepository busRepository;
    private final GarageRepository garageRepository;
    private final DataRetrievalLogRepository dataRetrievalLogRepository;
    private final SoapUtils soapUtils;
    
    @Value("${iett.soap.bus-service-url}")
    private String serviceUrl;
    
    @Value("${iett.soap.bus-method}")
    private String methodName;
    
    @Value("${iett.soap.data-cache-duration-minutes}")
    private int cacheDurationMinutes;
    
    public BusSoapService(
            RestTemplate restTemplate,
            BusRepository busRepository,
            GarageRepository garageRepository,
            DataRetrievalLogRepository dataRetrievalLogRepository,
            SoapUtils soapUtils) {
        this.restTemplate = restTemplate;
        this.busRepository = busRepository;
        this.garageRepository = garageRepository;
        this.dataRetrievalLogRepository = dataRetrievalLogRepository;
        this.soapUtils = soapUtils;
    }
    
    public List<Bus> getBusData() {
        boolean needsRefresh = needsDataRefresh();
        
        if (!needsRefresh) {
            log.info("Using cached bus data from database");
            return busRepository.findAll();
        }
        
        log.info("Fetching fresh bus data from SOAP service");
        List<SoapBusDTO> busData = fetchBusDataFromSoap();
        
        if (!busData.isEmpty()) {
            updateBusDatabase(busData);
            return busRepository.findAll();
        }
        
        return busRepository.findAll();
    }

    private boolean needsDataRefresh() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(cacheDurationMinutes);
        
        return !dataRetrievalLogRepository.existsRecentSuccessfulRetrieval(
                DataRetrievalLog.DataType.BUS, threshold);
    }
    

    private List<SoapBusDTO> fetchBusDataFromSoap() {
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
            
            return soapUtils.parseJsonToList(jsonData, new TypeReference<List<SoapBusDTO>>() {});
        } catch (Exception e) {
            log.error("Error fetching bus data: {}", e.getMessage(), e);
            logDataRetrieval(false, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private void updateBusDatabase(List<SoapBusDTO> busDTOs) {
        busRepository.deleteAll();
        
        List<Garage> garages = garageRepository.findAll();
        Map<String, Garage> garageMap = garages.stream()
                .collect(Collectors.toMap(Garage::getGarageCode, Function.identity()));
        
        List<Bus> buses = busDTOs.stream()
                .map(dto -> mapToBusEntity(dto, garageMap))
                .collect(Collectors.toList());
        
        busRepository.saveAll(buses);
        log.info("Saved {} buses to database", buses.size());
    }
    
    private Bus mapToBusEntity(SoapBusDTO dto, Map<String, Garage> garageMap) {
        Bus bus = new Bus();
        
        if (dto.getLicensePlate() != null && !dto.getLicensePlate().isEmpty()) {
            bus.setId(dto.getLicensePlate().hashCode());
            bus.setLicensePlate(dto.getLicensePlate());
        } else {
            bus.setId((int) (Math.random() * 1000000));
            bus.setLicensePlate("Unknown");
        }
        
        bus.setOperator(dto.getOperator());
        bus.setGarageCode(dto.getGarage());
        bus.setDoorNumber(dto.getDoorNo());
        
        bus.setRecordTime(soapUtils.parseTimeStringToDateTime(dto.getTime()));
        
        Double longitude = null;
        Double latitude = null;
        
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            longitude = soapUtils.parseStringToDouble(dto.getLongitude());
            latitude = soapUtils.parseStringToDouble(dto.getLatitude());
            
            if (longitude != null && latitude != null) {
                bus.setLongitude(longitude);
                bus.setLatitude(latitude);
                
                bus.setCoordinate(latitude + "," + longitude);
                
                calculateNearestGarage(bus, latitude, longitude, garageMap);
            }
        }
        
        if (dto.getSpeed() != null) {
            Double speed = soapUtils.parseStringToDouble(dto.getSpeed());
            bus.setSpeed(speed != null ? speed : 0.0);
        }
        
        bus.setLastUpdated(LocalDateTime.now());
        return bus;
    }
    
    private void calculateNearestGarage(Bus bus, double busLat, double busLon, Map<String, Garage> garageMap) {
        double minDistance = Double.MAX_VALUE;
        Garage nearestGarage = null;
        
        for (Garage garage : garageMap.values()) {
            if (garage.getCoordinate() == null) {
                continue;
            }
            
            String[] coordinates = garage.getCoordinate().split(",");
            if (coordinates.length != 2) {
                continue;
            }
            
            double garageLat = Double.parseDouble(coordinates[0]);
            double garageLon = Double.parseDouble(coordinates[1]);
            
            double distance = soapUtils.calculateDistance(busLat, busLon, garageLat, garageLon);
            
            if (distance < minDistance) {
                minDistance = distance;
                nearestGarage = garage;
            }
        }
        
        if (nearestGarage != null) {
            bus.setNearestGarageCode(nearestGarage.getGarageCode());
            bus.setNearestGarageName(nearestGarage.getGarageName());
            bus.setDistanceToNearestGarage(minDistance);
        }
    }
    
    private void logDataRetrieval(boolean success, String errorMessage) {
        DataRetrievalLog log = new DataRetrievalLog();
        log.setDataType(DataRetrievalLog.DataType.BUS);
        log.setRetrievalTime(LocalDateTime.now());
        log.setSuccess(success);
        log.setErrorMessage(errorMessage);
        
        dataRetrievalLogRepository.save(log);
    }
} 