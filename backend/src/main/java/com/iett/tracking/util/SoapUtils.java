package com.iett.tracking.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class SoapUtils {
    
    private static final Pattern POINT_PATTERN = Pattern.compile("POINT\\s*\\(\\s*([\\d.-]+)\\s+([\\d.-]+)\\s*\\)");
    private final ObjectMapper objectMapper;
    
    public SoapUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Parse JSON string to a list of objects
     * @param json The JSON string
     * @param typeReference TypeReference for the list of objects
     * @return List of objects
     * @param <T> The type of objects in the list
     */
    public <T> List<T> parseJsonToList(String json, TypeReference<List<T>> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            log.error("Empty JSON string received");
            return Collections.emptyList();
        }
        
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            JsonNode arrayNode = findArrayNode(jsonNode);
            
            if (arrayNode != null) {
                return objectMapper.readValue(arrayNode.toString(), typeReference);
            } else {
                log.error("No array found in JSON: {}", json);
                return Collections.emptyList();
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON to list: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Find the first array node in a JSON structure
     * @param node The JSON node to search
     * @return The first array node found, or null if none
     */
    private JsonNode findArrayNode(JsonNode node) {
        if (node.isArray()) {
            return node;
        }
        
        for (JsonNode child : node) {
            JsonNode arrayNode = findArrayNode(child);
            if (arrayNode != null) {
                return arrayNode;
            }
        }
        
        return null;
    }
    
    /**
     * Parse a coordinate string from "POINT (longitude latitude)" format to "latitude,longitude" format
     * @param pointString The coordinate string in POINT format
     * @return The coordinate string in "latitude,longitude" format
     */
    public String parsePointToCoordinate(String pointString) {
        if (pointString == null || pointString.trim().isEmpty()) {
            return null;
        }
        
        Matcher matcher = POINT_PATTERN.matcher(pointString);
        if (matcher.find()) {
            String longitude = matcher.group(1);
            String latitude = matcher.group(2);
            return latitude + "," + longitude;
        }
        
        log.warn("Could not parse POINT string: {}", pointString);
        return null;
    }
    
    /**
     * Parse a time string to a LocalDateTime
     * @param timeString The time string
     * @return A LocalDateTime object
     */
    public LocalDateTime parseTimeStringToDateTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(timeString, formatter);
        } catch (DateTimeParseException e) {
            log.warn("Could not parse time string: {}", timeString);
            return LocalDateTime.now();
        }
    }
    
    /**
     * Parse a string to a double
     * @param value The string value
     * @return The double value, or null if parsing fails
     */
    public Double parseStringToDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Could not parse string to double: {}", value);
            return null;
        }
    }
    
    /**
     * Calculate the distance between two geographical coordinates
     * @param lat1 Latitude of first coordinate
     * @param lon1 Longitude of first coordinate
     * @param lat2 Latitude of second coordinate
     * @param lon2 Longitude of second coordinate
     * @return The distance in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // The Haversine formula for calculating distance between two coordinates
        double earthRadius = 6371; // km
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return earthRadius * c;
    }
} 