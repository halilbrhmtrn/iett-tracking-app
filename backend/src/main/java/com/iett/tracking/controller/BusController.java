package com.iett.tracking.controller;

import com.iett.tracking.dto.BusDTO;
import com.iett.tracking.dto.SearchResponseDTO;
import com.iett.tracking.model.Bus;
import com.iett.tracking.repository.BusRepository;
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

    @Autowired
    public BusController(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    @GetMapping
    @Operation(summary = "Get a paginated list of buses", description = "Returns up to 20 buses per page from the database")
    public ResponseEntity<List<BusDTO>> getAllBuses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (size > 20) {
            size = 20;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("doorNo").ascending());
        Page<Bus> busPage = busRepository.findAll(pageable);
        
        List<BusDTO> busDTOs = busPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(busDTOs);
    }
    
    @GetMapping("/{doorNo}")
    @Operation(summary = "Get a bus by door number", description = "Returns a bus by its door number")
    public ResponseEntity<BusDTO> getBusByDoorNo(@PathVariable String doorNo) {
        Optional<Bus> busOpt = busRepository.findById(doorNo);
        
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
            bus.setOperator(busDTO.getOperator());
            bus.setGarage(busDTO.getGarage());
            bus.setLatitude(busDTO.getLatitude());
            bus.setLongitude(busDTO.getLongitude());
            bus.setSpeed(busDTO.getSpeed());
            bus.setLicensePlate(busDTO.getLicensePlate());
            bus.setTime(busDTO.getTime() != null ? busDTO.getTime() : LocalDateTime.now());
            bus.setLastUpdated(LocalDateTime.now());
            
            Bus savedBus = busRepository.save(bus);
            return new ResponseEntity<>(convertToDTO(savedBus), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{doorNo}")
    @Operation(summary = "Update a bus", description = "Updates an existing bus in the system")
    public ResponseEntity<BusDTO> updateBus(@PathVariable String doorNo, @RequestBody BusDTO busDTO) {
        try {
            Optional<Bus> busOpt = busRepository.findById(doorNo);
            
            if (busOpt.isPresent()) {
                Bus bus = busOpt.get();
                if (busDTO.getOperator() != null) bus.setOperator(busDTO.getOperator());
                if (busDTO.getGarage() != null) bus.setGarage(busDTO.getGarage());
                if (busDTO.getLatitude() != null) bus.setLatitude(busDTO.getLatitude());
                if (busDTO.getLongitude() != null) bus.setLongitude(busDTO.getLongitude());
                if (busDTO.getSpeed() != null) bus.setSpeed(busDTO.getSpeed());
                if (busDTO.getLicensePlate() != null) bus.setLicensePlate(busDTO.getLicensePlate());
                if (busDTO.getTime() != null) bus.setTime(busDTO.getTime());
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
    
    @DeleteMapping("/{doorNo}")
    @Operation(summary = "Delete a bus", description = "Deletes a bus from the system")
    public ResponseEntity<Void> deleteBus(@PathVariable String doorNo) {
        try {
            if (busRepository.existsById(doorNo)) {
                busRepository.deleteById(doorNo);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private BusDTO convertToDTO(Bus bus) {
        return BusDTO.builder()
                .doorNo(bus.getDoorNo())
                .operator(bus.getOperator())
                .garage(bus.getGarage())
                .latitude(bus.getLatitude())
                .longitude(bus.getLongitude())
                .speed(bus.getSpeed())
                .licensePlate(bus.getLicensePlate())
                .time(bus.getTime())
                .build();
    }
} 