package com.example.eventapp.payload;

import lombok.Data;
import java.util.List;

@Data
public class PaginatedResponse<T> {
    private List<T> results;
    private long totalResults;
    private int totalPages;
    private int page;
    private int pageSize;

    public PaginatedResponse(List<T> results, long totalResults, int totalPages, int page, int pageSize) {
        this.results = results;
        this.totalResults = totalResults;
        this.totalPages = totalPages;
        this.page = page;
        this.pageSize = pageSize;
    }
}
