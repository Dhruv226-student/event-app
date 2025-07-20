package com.example.eventapp.payload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ManagerUploadDto {

    private String type; // SINGLE or MULTIPLE

    private String title;
    private String description;
    private LocalDateTime eventDate; // 📅 Optional user-provided date

    private String location; // 📍 Optional user-provided location
    private List<MultipartFile> images;
    private List<MultipartFile> videos;
    
}
