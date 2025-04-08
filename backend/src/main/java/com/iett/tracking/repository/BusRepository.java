package com.iett.tracking.repository;

import com.iett.tracking.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Integer> {

    @Query("SELECT b FROM Bus b WHERE " +
            "LOWER(b.operator) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.garageCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.doorNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.licensePlate) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Bus> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    Optional<Bus> findByDoorNumber(String doorNumber);
} 