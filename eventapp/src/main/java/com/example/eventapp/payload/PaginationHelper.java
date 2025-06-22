package com.example.eventapp.payload;

// import com.example.eventapp.payload.PaginatedResponse;
import org.springframework.data.domain.Page;

public class PaginationHelper {

    public static <T> PaginatedResponse<T> buildResponse(Page<T> pageData) {
        return new PaginatedResponse<>(
            pageData.getContent(),
            pageData.getTotalElements(),
            pageData.getTotalPages(),
            pageData.getNumber()+1,
            pageData.getSize()
        );
    }
}
