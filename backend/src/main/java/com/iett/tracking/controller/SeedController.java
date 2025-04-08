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
        response.put("garages", garages.size());
        response.put("buses", buses.size());
        
        return ResponseEntity.ok(response);
    }
    
    private List<Garage> createDummyGarages() {
        List<Garage> garages = new ArrayList<>();
        
        garages.add(new Garage(1L, "İkitelli Garajı", "IKT", "41.062763,28.796153", LocalDateTime.now()));
        garages.add(new Garage(2L, "Edirnekapi Garajı", "EDK", "41.027978,28.940541", LocalDateTime.now()));
        garages.add(new Garage(3L, "Hasanpaşa Garajı", "HSP", "40.992283,29.036486", LocalDateTime.now()));
        garages.add(new Garage(4L, "Sarıgazi Garajı", "SRG", "41.004786,29.164681", LocalDateTime.now()));
        garages.add(new Garage(5L, "Anadolu Garajı", "AND", "41.018436,29.071863", LocalDateTime.now()));
        
        return garages;
    }
    
    private List<Bus> createDummyBuses(List<Garage> garages) {
        List<Bus> buses = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 1; i <= 20; i++) {
            Garage garage = garages.get(ThreadLocalRandom.current().nextInt(garages.size()));
            
            String[] coordinates = garage.getCoordinate().split(",");
            double baseLat = Double.parseDouble(coordinates[0]);
            double baseLng = Double.parseDouble(coordinates[1]);
            
            double lat = baseLat + (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.05;
            double lng = baseLng + (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.05;
            double speed = ThreadLocalRandom.current().nextDouble(0, 80);
            String licensePlate = "34 ABC " + i;
            LocalDateTime recordTime = now.minusMinutes(ThreadLocalRandom.current().nextInt(60));
            
            Bus bus = new Bus();
            bus.setId(i);
            bus.setDoorNumber("DOOR" + i);
            bus.setOperator("Operator" + (i % 3 + 1));
            bus.setGarageCode(garage.getGarageCode());
            bus.setLatitude(lat);
            bus.setLongitude(lng);
            bus.setSpeed(speed);
            bus.setLicensePlate(licensePlate);
            bus.setRecordTime(recordTime);
            bus.setLastUpdated(now);
            bus.setCoordinate(lat + "," + lng);
            
            bus.setNearestGarageCode(garage.getGarageCode());
            bus.setNearestGarageName(garage.getGarageName());
            bus.setDistanceToNearestGarage(0.0);
            
            buses.add(bus);
        }
        
        return buses;
    }
} 