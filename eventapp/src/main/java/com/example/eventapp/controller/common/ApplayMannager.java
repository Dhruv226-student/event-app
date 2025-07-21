package com.example.eventapp.controller.common;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.eventapp.services.cloud.CloudinaryService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.eventapp.model.TeamApplication;
import com.example.eventapp.payload.ApiResponse;
import com.example.eventapp.payload.ApplicationDto;
import com.example.eventapp.repository.ApplayMangerRepository;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/v1/common")
public class ApplayMannager {

    private final CloudinaryService cloudinaryService;

    private final ApplayMangerRepository applayMangerRepository;

    public ApplayMannager(ApplayMangerRepository applayMangerRepository, CloudinaryService cloudinaryService) {
        this.applayMangerRepository = applayMangerRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/create", consumes = { "multipart/form-data" })
    public ResponseEntity<ApiResponse<TeamApplication>> applyManager(
            @ModelAttribute @Valid ApplicationDto.ApplayManger request) throws IOException {

        if (applayMangerRepository.existsByEmail(request.getEmail())) {
            ApiResponse<TeamApplication> errorResponse = new ApiResponse<>(false, "This Email already registered",
                    null);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }

        MultipartFile logo = request.getLogo();
        String logoUrl = null;
        String publicId = null;

        if (logo != null && !logo.isEmpty()) {

            Map<String, Object> uploadResult = (Map<String, Object>) cloudinaryService.uploadFile(logo, "ManagerMedia");
            System.out.println("Upload Result: " + uploadResult);

            if (uploadResult != null) {
                logoUrl = uploadResult.get("url").toString();
                publicId = uploadResult.get("public_id").toString(); // ✅ capture publicId
            }
        }

        TeamApplication application = new TeamApplication();
        application.setTeamName(request.getTeamName());
        application.setEmail(request.getEmail());
        application.setPortfolioUrl(request.getPortfolioUrl());
        application.setDescription(request.getDescription());
        application.setInsta(request.getInsta());
        application.setFacebook(request.getFacebook());
        application.setStatus("PENDING");
        application.setCreatedAt(new Date());
        application.setLogoUrl(logoUrl);
        application.setLogoPublicId(publicId); // ✅ Save publicId

        TeamApplication saved = applayMangerRepository.save(application);

        ApiResponse<TeamApplication> successResponse = new ApiResponse<>(true, "Application submitted successfully",
                saved);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON).body(successResponse);
    }

}
