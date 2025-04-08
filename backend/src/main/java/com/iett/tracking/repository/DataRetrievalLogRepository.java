package com.iett.tracking.repository;

import com.iett.tracking.model.DataRetrievalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DataRetrievalLogRepository extends JpaRepository<DataRetrievalLog, Long> {
    
    /**
     * Check if there is a recent successful retrieval for the given data type after the given threshold time
     * @param dataType The data type to check
     * @param threshold The threshold time
     * @return True if there is a recent successful retrieval, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM DataRetrievalLog l " +
           "WHERE l.dataType = :dataType AND l.success = true AND l.retrievalTime > :threshold")
    boolean existsRecentSuccessfulRetrieval(@Param("dataType") DataRetrievalLog.DataType dataType,
                                           @Param("threshold") LocalDateTime threshold);
    
    /**
     * Find the most recent retrieval log for the given data type
     * @param dataType The data type to find
     * @return The most recent retrieval log
     */
    @Query("SELECT l FROM DataRetrievalLog l WHERE l.dataType = :dataType ORDER BY l.retrievalTime DESC")
    DataRetrievalLog findMostRecent(@Param("dataType") DataRetrievalLog.DataType dataType);
} 