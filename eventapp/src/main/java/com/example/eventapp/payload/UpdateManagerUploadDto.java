package com.example.eventapp.payload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateManagerUploadDto {

    private String title;
    private String description;
    private LocalDateTime eventDate;

    private List<MultipartFile> newImages;
    private List<MultipartFile> newVideos;

    private List<String> deleteMediaPublicIds; // Media to delete
}
