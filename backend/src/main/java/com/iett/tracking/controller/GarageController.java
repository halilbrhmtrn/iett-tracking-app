package com.iett.tracking.controller;

import com.iett.tracking.dto.GarageDTO;
import com.iett.tracking.dto.SearchResponseDTO;
import com.iett.tracking.model.Garage;
import com.iett.tracking.repository.GarageRepository;
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
@RequestMapping("/api/garages")
@Tag(name = "Garage", description = "Garage management APIs")
public class GarageController {

    private final GarageRepository garageRepository;

    @Autowired
    public GarageController(GarageRepository garageRepository) {
        this.garageRepository = garageRepository;
    }

    @GetMapping
    @Operation(summary = "Get a paginated list of garages", description = "Returns up to 20 garages per page from the database")
    public ResponseEntity<List<GarageDTO>> getAllGarages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (size > 20) {
            size = 20;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Garage> garagePage = garageRepository.findAll(pageable);
        
        List<GarageDTO> garageDTOs = garagePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(garageDTOs);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a garage by ID", description = "Returns a garage by its ID")
    public ResponseEntity<GarageDTO> getGarageById(@PathVariable Long id) {
        Optional<Garage> garageOpt = garageRepository.findById(id);
        
        if (garageOpt.isPresent()) {
            return ResponseEntity.ok(convertToDTO(garageOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search garages", description = "Search garages by ID, name, or code (max 20 results)")
    public ResponseEntity<SearchResponseDTO<GarageDTO>> searchGarages(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (size > 20) {
            size = 20;
        }
        
        List<Garage> garages = garageRepository.findBySearchTerm(term);
        
        int start = page * size;
        int end = Math.min(start + size, garages.size());
        
        List<Garage> paginatedGarages = (start < end) 
            ? garages.subList(start, end) 
            : List.of();
        
        List<GarageDTO> garageDTOs = paginatedGarages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        SearchResponseDTO<GarageDTO> response = SearchResponseDTO.<GarageDTO>builder()
                .results(garageDTOs)
                .count(garageDTOs.size())
                .totalCount(garages.size())
                .page(page)
                .size(size)
                .searchTerm(term)
                .hasMatches(!garageDTOs.isEmpty())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create a new garage", description = "Creates a new garage in the system")
    public ResponseEntity<GarageDTO> createGarage(@RequestBody GarageDTO garageDTO) {
        try {
            Garage garage = new Garage();
            garage.setGarageName(garageDTO.getGarageName());
            garage.setGarageCode(garageDTO.getGarageCode());
            garage.setCoordinate(garageDTO.getCoordinate());
            garage.setLastUpdated(LocalDateTime.now());
            
            Garage savedGarage = garageRepository.save(garage);
            
            return new ResponseEntity<>(convertToDTO(savedGarage), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a garage", description = "Updates an existing garage in the system")
    public ResponseEntity<GarageDTO> updateGarage(@PathVariable Long id, @RequestBody GarageDTO garageDTO) {
        try {
            Optional<Garage> garageOpt = garageRepository.findById(id);
            
            if (garageOpt.isPresent()) {
                Garage garage = garageOpt.get();
                garage.setGarageName(garageDTO.getGarageName());
                garage.setGarageCode(garageDTO.getGarageCode());
                garage.setCoordinate(garageDTO.getCoordinate());
                garage.setLastUpdated(LocalDateTime.now());
                
                Garage updatedGarage = garageRepository.save(garage);
                
                return ResponseEntity.ok(convertToDTO(updatedGarage));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a garage", description = "Deletes a garage from the system")
    public ResponseEntity<Void> deleteGarage(@PathVariable Long id) {
        try {
            if (garageRepository.existsById(id)) {
                garageRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private GarageDTO convertToDTO(Garage garage) {
        return GarageDTO.builder()
                .id(garage.getId())
                .garageName(garage.getGarageName())
                .garageCode(garage.getGarageCode())
                .coordinate(garage.getCoordinate())
                .build();
    }
} 