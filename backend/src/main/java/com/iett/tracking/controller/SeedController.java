package com.iett.tracking.controller;

import com.iett.tracking.model.Bus;
import com.iett.tracking.model.Garage;
import com.iett.tracking.repository.BusRepository;
import com.iett.tracking.repository.GarageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/seed")
@Tag(name = "Seed", description = "For development testing - seed the database with dummy data")
public class SeedController {

    private final GarageRepository garageRepository;
    private final BusRepository busRepository;

    @Autowired
    public SeedController(GarageRepository garageRepository, BusRepository busRepository) {
        this.garageRepository = garageRepository;
        this.busRepository = busRepository;
    }

    @PostMapping
    @Operation(summary = "Seed database with dummy data", description = "Creates sample garages and buses for testing")
    public ResponseEntity<Map<String, Object>> seedDatabase() {
        busRepository.deleteAll();
        garageRepository.deleteAll();
        
        List<Garage> garages = createDummyGarages();
        garageRepository.saveAll(garages);
        
        List<Bus> buses = createDummyBuses(garages);
        busRepository.saveAll(buses);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("garagesCreated", garages.size());
        response.put("busesCreated", buses.size());
        response.put("message", "Database seeded successfully with dummy data");
        
        return ResponseEntity.ok(response);
    }
    
    private List<Garage> createDummyGarages() {
        List<Garage> garages = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        garages.add(new Garage(null, "İkitelli Garage", "IKT", "41.0575,28.7783", now));
        garages.add(new Garage(null, "Edirnekapı Garage", "EDK", "41.0332,28.9356", now));
        garages.add(new Garage(null, "Hasanpaşa Garage", "HSP", "40.9944,29.0368", now));
        garages.add(new Garage(null, "Anadolu Garage", "ANL", "41.0119,29.0713", now));
        garages.add(new Garage(null, "Ayazağa Garage", "AYZ", "41.1071,29.0075", now));
        garages.add(new Garage(null, "Kağıthane Garage", "KGT", "41.0694,28.9719", now));
        garages.add(new Garage(null, "Sarıgazi Garage", "SRG", "41.0178,29.1871", now));
        garages.add(new Garage(null, "Topkapı Garage", "TPK", "41.0147,28.9347", now));
        garages.add(new Garage(null, "Tuzla Garage", "TZL", "40.8158,29.3009", now));
        garages.add(new Garage(null, "Yunus Garage", "YNS", "40.8889,29.1869", now));
        
        return garages;
    }
    
    private List<Bus> createDummyBuses(List<Garage> garages) {
        List<Bus> buses = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 1; i <= 30; i++) {
            Garage garage = garages.get(ThreadLocalRandom.current().nextInt(garages.size()));
            
            String[] coordinates = garage.getCoordinate().split(",");
            double baseLat = Double.parseDouble(coordinates[0]);
            double baseLng = Double.parseDouble(coordinates[1]);
            
            double lat = baseLat + (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.05;
            double lng = baseLng + (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.05;
            
            Bus bus = new Bus(
                    "DOOR" + i,
                    "Operator" + (i % 3 + 1),
                    garage.getGarageCode(),
                    lat,
                    lng,
                    ThreadLocalRandom.current().nextDouble(0, 80),
                    "34 ABC " + i,
                    now.minusMinutes(ThreadLocalRandom.current().nextInt(60)),
                    now
            );
            
            buses.add(bus);
        }
        
        return buses;
    }
} 