package com.example.eventapp.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.eventapp.model.TeamApplication;

public interface ApplayMangerRepository extends MongoRepository<TeamApplication, String> {
 
    Optional<TeamApplication> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndStatus(String email, String status);

 Optional<TeamApplication> findByUserId(String userId);
    // Optional<TeamApplication> findByIdAndUserId(String id, String userId);
}
