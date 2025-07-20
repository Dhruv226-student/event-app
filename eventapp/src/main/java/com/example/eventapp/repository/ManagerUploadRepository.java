package com.example.eventapp.repository;

import com.example.eventapp.model.ManagerUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ManagerUploadRepository extends MongoRepository<ManagerUpload, String> {

    List<ManagerUpload> findByManagerId(String managerId);
    Page<ManagerUpload> findByManagerId(String managerId, Pageable pageable);

    // ✅ Fix this line:
    long countByManagerId(Object managerId);

    List<ManagerUpload> findByType(String type);
    List<ManagerUpload> findByTypeAndManagerId(String type, String managerId);
    // List<ManagerUpload> findByTypeAndStatus(String type, String status);
    // List<ManagerUpload> findByTypeAndStatusAndManagerId(String type, String managerId);
}
