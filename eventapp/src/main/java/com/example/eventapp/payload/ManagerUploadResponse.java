package com.example.eventapp.payload;

import com.example.eventapp.model.MediaItem;
import com.example.eventapp.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ManagerUploadResponse {
    private String id;
    private User manager; // Full user details
    private String type;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<MediaItem> media;
}
