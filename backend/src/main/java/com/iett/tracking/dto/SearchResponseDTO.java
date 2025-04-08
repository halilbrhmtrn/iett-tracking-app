package com.iett.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDTO<T> {
    private List<T> results;
    private int count;
    private int totalCount;
    private int page;
    private int size;
    private String searchTerm;
    private boolean hasMatches;
} 