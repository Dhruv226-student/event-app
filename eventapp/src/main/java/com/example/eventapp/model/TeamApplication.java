package com.example.eventapp.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;

import java.util.Date;

@Document(collection = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamApplication {

    @Id
    private String id;
    private String username;
    private String teamName;
    private String email;
    private String description;
    private String portfolioUrl;
    private String insta;
    private String facebook;
    private String status; // PENDING | APPROVED | REJECTED
     private ObjectId userId;
     private String logoUrl; // NEW: Cloudinary URL of uploaded logo
    private String logoPublicId; // NEW: Cloudinary public ID for logo
    @CreatedDate
    private Date createdAt;

    @LastModifiedDate

    private Date updatedAt;

        private Date deletedAt; // For soft delete functionality
    // Getters and setters or use Lombok (@Getter, @Setter)
}
