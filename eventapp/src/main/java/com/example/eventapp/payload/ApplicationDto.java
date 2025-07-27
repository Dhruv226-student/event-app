package com.example.eventapp.payload;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class ApplicationDto {

    @Data
    public static class ApplayManger {

        @NotBlank
        private String teamName;
        @NotBlank
        private String username;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String description;

        private String portfolioUrl;

        private String insta;

        private String facebook;
        
        private MultipartFile logo;
    }

    @Data
    public static class UpdateApplayManger {

        @NotBlank
        private String teamName;

        @NotBlank
        private String description;

        private String portfolioUrl;

        private String insta;

        private String facebook;

        private MultipartFile logo;
    }



    @Data
    public static class ManagerUploadDto {

        @NotBlank(message = "Upload type is required")
        private String type; // SINGLE | MULTIPLE

        private String title;
        private String description;

        private List<MultipartFile> images;
        private List<MultipartFile> videos;
    }

}
