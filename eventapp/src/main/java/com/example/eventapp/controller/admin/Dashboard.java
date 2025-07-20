package com.example.eventapp.controller.admin;

import com.example.eventapp.model.User;
import com.example.eventapp.repository.ApplayMangerRepository;
import com.example.eventapp.repository.ManagerUploadRepository;
import com.example.eventapp.repository.UserRepository;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/admin/dashboard")
public class Dashboard {

    private final ApplayMangerRepository applayMangerRepository;
    private final UserRepository userRepository;
    private final ManagerUploadRepository eventRepository;

    public Dashboard(ApplayMangerRepository applayMangerRepository, UserRepository userRepository, ManagerUploadRepository eventRepository) {
        this.applayMangerRepository = applayMangerRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        long totalApplications = applayMangerRepository.count();
        long approvedApplications = applayMangerRepository.countByStatus("APPROVED");
        long rejectedApplications = applayMangerRepository.countByStatus("REJECTED");


        long totalManagers = userRepository.countByRole("MANAGER");
        // long approvedManagers = userRepository.countByRoleAndApproved("MANAGER", true);

        // Example: Count events created by each manager (performance)
        List<User> managers = userRepository.findByRole("MANAGER");
        
        if (managers.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

    List<Map<String, Object>> performance = managers.stream().map(manager -> {
    Map<String, Object> map = new HashMap<>();
System.out.println(manager.getId());
    map.put("managerId", manager.getId());
    map.put("email", manager.getEmail());
    ObjectId id = new ObjectId(manager.getId());
    map.put("eventsCount", eventRepository.countByManagerId(id));
    return map;
}).collect(Collectors.toList());


        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", totalApplications);
        stats.put("approvedApplications", approvedApplications);
        stats.put("rejectedApplications", rejectedApplications);
        stats.put("totalManagers", totalManagers);

        // stats.put("approvedManagers", approvedManagers);
        stats.put("managerPerformance", performance);

        return ResponseEntity.ok(stats);
    }
}
