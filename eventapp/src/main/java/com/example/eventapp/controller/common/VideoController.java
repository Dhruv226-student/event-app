package com.example.eventapp.controller.common;

import com.example.eventapp.model.ManagerUpload;
import com.example.eventapp.model.User;
import com.example.eventapp.payload.ApiResponse;
import com.example.eventapp.payload.ManagerUploadResponse;
import com.example.eventapp.payload.PaginatedResponse;
import com.example.eventapp.payload.PaginationHelper;
import com.example.eventapp.repository.ManagerUploadRepository;
import com.example.eventapp.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/common/videos")
public class VideoController {

    private final ManagerUploadRepository managerUploadRepository;

    public VideoController(ManagerUploadRepository managerUploadRepository) {
        this.managerUploadRepository = managerUploadRepository;
    }

@Autowired
private UserRepository userRepository; // Make sure this exists

@GetMapping
public ResponseEntity<ApiResponse<PaginatedResponse<ManagerUploadResponse>>> getAllVideos(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
    Page<ManagerUpload> uploadsPage = managerUploadRepository.findAll(pageable);

    List<ManagerUploadResponse> enrichedResults = uploadsPage.getContent().stream().map(upload -> {
    User manager = userRepository.findById(upload.getManagerId().toString()).orElse(null);

    return ManagerUploadResponse.builder()
            .id(upload.getId())
            .manager(manager)
            .type(upload.getType())
            .title(upload.getTitle())
            .description(upload.getDescription())
            .eventDate(upload.getEventDate())
            .location(upload.getLocation())
            .createdAt(upload.getCreatedAt())
            .updatedAt(upload.getUpdatedAt())
            .deletedAt(upload.getDeletedAt())
            .media(upload.getMedia())
            .build();
}).toList();
PaginatedResponse<ManagerUploadResponse> paginatedResponse = new PaginatedResponse<>(
    enrichedResults,
    uploadsPage.getTotalElements(),     // totalResults
    uploadsPage.getTotalPages(),        // totalPages
    uploadsPage.getNumber() + 1,        // page (1-based)
    uploadsPage.getSize()               // pageSize
);


    ApiResponse<PaginatedResponse<ManagerUploadResponse>> response = new ApiResponse<>(
            true, "Videos fetched successfully", paginatedResponse);

    return ResponseEntity.ok(response);
}

}
                    