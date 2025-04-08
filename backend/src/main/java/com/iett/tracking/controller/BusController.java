package com.iett.tracking.controller;

import com.iett.tracking.dto.BusDTO;
import com.iett.tracking.dto.SearchResponseDTO;
import com.iett.tracking.model.Bus;
import com.iett.tracking.model.Garage;
import com.iett.tracking.repository.BusRepository;
import com.iett.tracking.repository.GarageRepository;
import com.iett.tracking.service.BusSoapService;
import com.iett.tracking.util.SoapUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/buses")
@Tag(name = "Bus", description = "Bus management APIs")
public class BusController {

    private final BusRepository busRepository;
    private final BusSoapService busSoapService;
    private final SoapUtils soapUtils;
    private final GarageRepository garageRepository;

    @Autowired
    public BusController(BusRepository busRepository, BusSoapService busSoapService, SoapUtils soapUtils, GarageRepository garageRepository) {
        this.busRepository = busRepository;
        this.busSoapService = busSoapService;
        this.soapUtils = soapUtils;
        this.garageRepository = garageRepository;
    }

    @GetMapping
    @Operation(summary = "Get a paginated list of buses", description = "Returns up to 20 buses per page from the database")
    public ResponseEntity<List<BusDTO>> getAllBuses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (size > 20) {
            size = 20;
        }
        
        // First, ensure we have up-to-date data from SOAP service if needed
        busSoapService.getBusData();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("doorNo").ascending());
        Page<Bus> busPage = busRepository.findAll(pageable);
        
        List<BusDTO> busDTOs = busPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(busDTOs);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a bus by ID", description = "Returns a bus by its ID")
    public ResponseEntity<BusDTO> getBusById(@PathVariable Integer id) {
        busSoapService.getBusData();
        
        Optional<Bus> busOpt = busRepository.findById(id);
        
        if (busOpt.isPresent()) {
            return ResponseEntity.ok(convertToDTO(busOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search buses", description = "Search buses by door number, operator, garage, or license plate (max 20 results)")
    public ResponseEntity<SearchResponseDTO<BusDTO>> searchBuses(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (size > 20) {
            size = 20;
        }
        
        busSoapService.getBusData();
        
        List<Bus> buses = busRepository.findBySearchTerm(term);
        
        int start = page * size;
        int end = Math.min(start + size, buses.size());
        
        List<Bus> paginatedBuses = (start < end) 
            ? buses.subList(start, end) 
            : List.of();
        
        List<BusDTO> busDTOs = paginatedBuses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        SearchResponseDTO<BusDTO> response = SearchResponseDTO.<BusDTO>builder()
                .results(busDTOs)
                .count(busDTOs.size())
                .totalCount(buses.size())
                .page(page)
                .size(size)
                .searchTerm(term)
                .hasMatches(!busDTOs.isEmpty())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create a new bus", description = "Creates a new bus in the system")
    public ResponseEntity<BusDTO> createBus(@RequestBody BusDTO busDTO) {
        try {
            if (busDTO.getDoorNo() == null || busDTO.getDoorNo().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Bus bus = new Bus();
            bus.setDoorNo(busDTO.getDoorNo());
            bus.setDoorNumber(busDTO.getDoorNo());
            bus.setOperator(busDTO.getOperator());
            bus.setGarageCode(busDTO.getGarage());
            bus.setLatitude(busDTO.getLatitude());
            bus.setLongitude(busDTO.getLongitude());
            
            if (busDTO.getLatitude() != null && busDTO.getLongitude() != null) {
                bus.setCoordinate(busDTO.getLatitude() + "," + busDTO.getLongitude());
                
                calculateNearestGarage(bus);
            }
            
            bus.setSpeed(busDTO.getSpeed());
            bus.setLicensePlate(busDTO.getLicensePlate());
            bus.setTime(busDTO.getTime() != null ? busDTO.getTime() : LocalDateTime.now());
            bus.setRecordTime(bus.getTime());
            bus.setLastUpdated(LocalDateTime.now());
            
            if (bus.getId() == null) {
                if (bus.getLicensePlate() != null && !bus.getLicensePlate().isEmpty()) {
                    bus.setId(bus.getLicensePlate().hashCode());
                } else {
                    bus.setId((int) (Math.random() * 1000000));
                }
            }
            
            Bus savedBus = busRepository.save(bus);
            return new ResponseEntity<>(convertToDTO(savedBus), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a bus", description = "Updates an existing bus in the system")
    public ResponseEntity<BusDTO> updateBus(@PathVariable Integer id, @RequestBody BusDTO busDTO) {
        try {
            Optional<Bus> busOpt = busRepository.findById(id);
            
            if (busOpt.isPresent()) {
                Bus bus = busOpt.get();
                if (busDTO.getOperator() != null) bus.setOperator(busDTO.getOperator());
                if (busDTO.getGarage() != null) bus.setGarageCode(busDTO.getGarage());
                
                boolean coordinateUpdated = false;
                if (busDTO.getLatitude() != null) {
                    bus.setLatitude(busDTO.getLatitude());
                    coordinateUpdated = true;
                }
                if (busDTO.getLongitude() != null) {
                    bus.setLongitude(busDTO.getLongitude());
                    coordinateUpdated = true;
                }
                
                if (coordinateUpdated && bus.getLatitude() != null && bus.getLongitude() != null) {
                    bus.setCoordinate(bus.getLatitude() + "," + bus.getLongitude());
                    
                    calculateNearestGarage(bus);
                }
                
                if (busDTO.getSpeed() != null) bus.setSpeed(busDTO.getSpeed());
                if (busDTO.getLicensePlate() != null) bus.setLicensePlate(busDTO.getLicensePlate());
                if (busDTO.getTime() != null) {
                    bus.setTime(busDTO.getTime());
                    bus.setRecordTime(busDTO.getTime());
                }
                bus.setLastUpdated(LocalDateTime.now());
                
                Bus updatedBus = busRepository.save(bus);
                
                return ResponseEntity.ok(convertToDTO(updatedBus));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private void calculateNearestGarage(Bus bus) {
        if (bus.getLatitude() == null || bus.getLongitude() == null) {
            return;
        }
        
        List<Garage> garages = garageRepository.findAll();
        if (garages.isEmpty()) {
            return;
        }
        
        double minDistance = Double.MAX_VALUE;
        Garage nearestGarage = null;
        
        for (Garage garage : garages) {
            if (garage.getCoordinate() == null) {
                continue;
            }
            
            String[] coordinates = garage.getCoordinate().split(",");
            if (coordinates.length != 2) {
                continue;
            }
            
            try {
                double garageLat = Double.parseDouble(coordinates[0]);
                double garageLon = Double.parseDouble(coordinates[1]);
                
                double distance = soapUtils.calculateDistance(
                    bus.getLatitude(), bus.getLongitude(), garageLat, garageLon);
                
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestGarage = garage;
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        
        if (nearestGarage != null) {
            bus.setNearestGarageCode(nearestGarage.getGarageCode());
            bus.setNearestGarageName(nearestGarage.getGarageName());
            bus.setDistanceToNearestGarage(minDistance);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bus", description = "Deletes a bus from the system")
    public ResponseEntity<Void> deleteBus(@PathVariable Integer id) {
        try {
            if (busRepository.existsById(id)) {
                busRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/refresh")
    @Operation(summary = "Force refresh of bus data", description = "Forces a refresh of bus data from the SOAP service")
    public ResponseEntity<List<BusDTO>> refreshBusData() {
        List<Bus> buses = busSoapService.getBusData();
        List<BusDTO> busDTOs = buses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(busDTOs);
    }


    private BusDTO convertToDTO(Bus bus) {
        return BusDTO.builder()
                .doorNo(bus.getDoorNo())
                .operator(bus.getOperator())
                .garage(bus.getGarageCode())
                .latitude(bus.getLatitude())
                .longitude(bus.getLongitude())
                .speed(bus.getSpeed())
                .licensePlate(bus.getLicensePlate())
                .time(bus.getTime() != null ? bus.getTime() : bus.getRecordTime())
                .nearestGarageCode(bus.getNearestGarageCode())
                .nearestGarageName(bus.getNearestGarageName())
                .distanceToNearestGarage(bus.getDistanceToNearestGarage())
                .build();
    }
} 