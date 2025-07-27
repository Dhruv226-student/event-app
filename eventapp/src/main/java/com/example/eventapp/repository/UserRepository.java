package com.example.eventapp.repository;

import com.example.eventapp.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(String role);
    boolean existsByEmailAndRole(String email, String role);
    
    List<User> findByRole(String role); // ✅ Corrected method
    List<User> findById(User id);
    // long countByRoleAndApproved(String role, boolean approved);
    Optional<User> findByResetToken(String resetToken);
    Optional<User> findByIdAndRole(String id, String role);
}
