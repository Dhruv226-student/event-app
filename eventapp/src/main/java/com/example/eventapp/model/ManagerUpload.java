package com.example.eventapp.model;

import lombok.*;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "manager_uploads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerUpload {

    @Id
    private String id;

    private ObjectId managerId; // Who uploaded this

    private String type;

    private String title;
    private String description;

    private LocalDateTime eventDate; // 📅 Optional user-provided date
    private String location; // 📍 Optional user-provided location
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private List<MediaItem> media;
}
