package com.iett.tracking.repository;

import com.iett.tracking.model.Garage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Long> {

    @Query("SELECT g FROM Garage g WHERE " +
            "LOWER(g.garageName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(g.garageCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CAST(g.id AS string) LIKE CONCAT('%', :searchTerm, '%')")
    List<Garage> findBySearchTerm(@Param("searchTerm") String searchTerm);
} 