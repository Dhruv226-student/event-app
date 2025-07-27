package com.example.eventapp.controller.user;

import com.example.eventapp.config.SecurityConfig.CurrentUser;
import com.example.eventapp.model.ManagerUpload;
import com.example.eventapp.model.MediaItem;
import com.example.eventapp.model.User;
import com.example.eventapp.payload.ApiResponse;
import com.example.eventapp.payload.ManagerUploadDto;
import com.example.eventapp.payload.UpdateManagerUploadDto;
import com.example.eventapp.repository.ManagerUploadRepository;
import com.example.eventapp.services.cloud.CloudinaryService;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/user/uploads")
public class ManagerUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ManagerUploadRepository managerUploadRepository;

   @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ApiResponse<ManagerUpload>> upload(
        @ModelAttribute ManagerUploadDto request,
        @CurrentUser User manager
) throws IOException {

    if (!"MANAGER".equals(manager.getRole())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Only managers can upload", null));
    }

    List<MediaItem> mediaItems = new ArrayList<>();
    String folder = "ManagerUploads/" + manager.getId() + "/";

    if (request.getImages() != null) {
        mediaItems.addAll(
            request.getImages().parallelStream().map(img -> {
                try {
                    Map<String, Object> result = cloudinaryService.uploadFile(img, folder);
                    return MediaItem.builder()
                            .type("image")
                            .url(result.get("url").toString())
                            .publicId(result.get("public_id").toString())
                            .build();
                } catch (IOException e) {
                    throw new RuntimeException("Image upload failed", e);
                }
            }).collect(Collectors.toList())
        );
    }

    if (request.getVideos() != null) {
        mediaItems.addAll(
            request.getVideos().parallelStream().map(vid -> {
                try {
                    Map<String, Object> result = cloudinaryService.uploadFile(vid, folder, "video");
                    return MediaItem.builder()
                            .type("video")
                            .url(result.get("url").toString())
                            .publicId(result.get("public_id").toString())
                            .build();
                } catch (IOException e) {
                    throw new RuntimeException("Video upload failed", e);
                }
            }).collect(Collectors.toList())
        );
    }

    ManagerUpload upload = ManagerUpload.builder()
            .managerId(manager.getId())
            .type(request.getType())
            .eventDate(request.getEventDate())
            .title(request.getTitle())
            .description(request.getDescription())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .location(request.getLocation())
            .media(mediaItems)
            .build();

    ManagerUpload saved = managerUploadRepository.save(upload);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, "Upload successful", saved));
}

  @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ApiResponse<ManagerUpload>> updateUpload(
        @PathVariable String id,
        @ModelAttribute UpdateManagerUploadDto request,
        @CurrentUser User user) throws IOException {

    Optional<ManagerUpload> optionalUpload = managerUploadRepository.findById(id);
    if (optionalUpload.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Upload not found", null));
    }

    ManagerUpload upload = optionalUpload.get();

    if (!user.getId().equals(upload.getManagerId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "You are not authorized", null));
    }

    List<MediaItem> updatedMedia = new ArrayList<>(upload.getMedia());

    if (request.getDeleteMediaPublicIds() != null) {
        for (String publicId : request.getDeleteMediaPublicIds()) {
            cloudinaryService.deleteFile(publicId);
            updatedMedia.removeIf(item -> item.getPublicId().equals(publicId)
                    || item.getPublicId().endsWith("/" + publicId));
        }
    }

    String folder = "ManagerUploads/" + upload.getManagerId() + "/";

    if (request.getNewImages() != null) {
        updatedMedia.addAll(
            request.getNewImages().parallelStream().map(img -> {
                try {
                    Map<String, Object> result = cloudinaryService.uploadFile(img, folder, "image");
                    return MediaItem.builder()
                            .type("image")
                            .url(result.get("url").toString())
                            .publicId(result.get("public_id").toString())
                            .build();
                } catch (IOException e) {
                    throw new RuntimeException("Image upload failed", e);
                }
            }).collect(Collectors.toList())
        );
    }

    if (request.getNewVideos() != null) {
        updatedMedia.addAll(
            request.getNewVideos().parallelStream().map(vid -> {
                try {
                    Map<String, Object> result = cloudinaryService.uploadFile(vid, folder, "video");
                    return MediaItem.builder()
                            .type("video")
                            .url(result.get("url").toString())
                            .publicId(result.get("public_id").toString())
                            .build();
                } catch (IOException e) {
                    throw new RuntimeException("Video upload failed", e);
                }
            }).collect(Collectors.toList())
        );
    }

    upload.setTitle(request.getTitle());
    upload.setDescription(request.getDescription());
    upload.setEventDate(request.getEventDate());
    upload.setMedia(updatedMedia);
    upload.setLocation(request.getLocation());
    upload.setUpdatedAt(LocalDateTime.now());

    ManagerUpload saved = managerUploadRepository.save(upload);

    return ResponseEntity.ok(new ApiResponse<>(true, "Upload updated successfully", saved));
}

   @GetMapping("/list")
public ResponseEntity<ApiResponse<Map<String, Object>>> listUploadsByManagerId(
        @CurrentUser User user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction) {

    if (!"MANAGER".equals(user.getRole())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Only managers can view uploads", null));
    }

    Sort sort = direction.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<ManagerUpload> uploads = managerUploadRepository.findByManagerId(user.getId(), pageable);

    Map<String, Object> response = new HashMap<>();
    response.put("items", uploads.getContent());
    response.put("currentPage", uploads.getNumber());
    response.put("totalItems", uploads.getTotalElements());
    response.put("totalPages", uploads.getTotalPages());

    return ResponseEntity.ok(new ApiResponse<>(true, "Upload list fetched", response));
}

    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<ManagerUpload>> getUploadDetailById(
            @CurrentUser User user,
            @PathVariable String id) {
        if (!"MANAGER".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Only managers can view uploads", null));
        }

        Optional<ManagerUpload> uploadOpt = managerUploadRepository.findById(id);
        if (uploadOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Upload not found", null));
        }

        ManagerUpload upload = uploadOpt.get();

        if (!user.getId().equals(upload.getManagerId()))
 {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "You are not authorized to view this upload", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Upload details fetched", upload));
    }

}