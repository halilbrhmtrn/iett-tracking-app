package com.iett.tracking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_retrieval_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataRetrievalLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataType dataType;
    
    @Column(nullable = false)
    private LocalDateTime retrievalTime;
    
    @Column(nullable = false)
    private boolean success;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    public enum DataType {
        GARAGE,
        BUS
    }
} 