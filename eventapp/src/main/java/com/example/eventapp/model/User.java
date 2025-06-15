package com.example.eventapp.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Id;

import lombok.*;

@Document(collection = "users") // MongoDB collection name
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {
    @Id
    private String id; // MongoDB will generate this ID
    private String username;
    private String email;
    @JsonIgnore // 👈 This hides password from all JSON serialization
    private String password;
    private String role; // e.g., "USER", "ADMIN"

    // Additional fields can be added as needed
    // For example, you might want to add firstName, lastName, etc.
}