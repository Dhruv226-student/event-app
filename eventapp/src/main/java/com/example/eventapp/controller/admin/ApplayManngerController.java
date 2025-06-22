package com.example.eventapp.controller.admin;

import com.example.eventapp.model.TeamApplication;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.ApplayMangerRepository;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.services.EmailService;
import com.example.eventapp.payload.ApiResponse;
import com.example.eventapp.payload.PaginatedResponse;
import com.example.eventapp.payload.PaginationHelper;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;

class PasswordUtil {
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

@RestController
@RequestMapping("/v1/admin/applications")
@PreAuthorize("hasRole('ADMIN')")
public class ApplayManngerController {

    private final EmailService emailService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ApplayMangerRepository applayMangerRepository;

    public ApplayManngerController(ApplayMangerRepository applayMangerRepository, PasswordEncoder passwordEncoder,
            UserRepository userRepository, EmailService emailService) {
        this.applayMangerRepository = applayMangerRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // ✅ Get all applications
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<TeamApplication>>> getPaginatedApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<TeamApplication> appPage = applayMangerRepository.findAll(pageable);

        PaginatedResponse<TeamApplication> pagination = PaginationHelper.buildResponse(appPage);

        ApiResponse<PaginatedResponse<TeamApplication>> response = new ApiResponse<>(true,
                "Paginated applications fetched", pagination);

        return ResponseEntity.ok(response);
    }

    // ✅ Get one by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamApplication>> getApplicationById(@PathVariable String id) {
        Optional<TeamApplication> application = applayMangerRepository.findById(id);
        if (application.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Application found", application.get()));
        }
        return ResponseEntity.status(404).body(new ApiResponse<>(false, "Application not found", null));
    }

    // ✅ Approve or reject (basic)
    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<TeamApplication>> updateStatus(
            @PathVariable String id,
            @RequestParam("status") String status) {
        Optional<TeamApplication> applicationOpt = applayMangerRepository.findById(id);
        if (applicationOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "Application not found", null));
        }

        TeamApplication application = applicationOpt.get();
        String normalizedStatus = status.toUpperCase();

        if (!normalizedStatus.equals("APPROVED") && !normalizedStatus.equals("REJECTED")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid status. Use 'APPROVED' or 'REJECTED'", null));
        }

        application.setStatus(normalizedStatus);
        applayMangerRepository.save(application);

        // If APPROVED, create user and send email
       if (normalizedStatus.equals("APPROVED")) {
    String randomPassword = PasswordUtil.generateRandomPassword(10);

    // 🔒 Save user
    User user = new User();
    user.setEmail(application.getEmail());
    user.setPassword(passwordEncoder.encode(randomPassword));
    user.setRole("MANAGER");
    userRepository.save(user);

    // 🔗 Link user ID to the application
   application.setUserId(new ObjectId(user.getId())); // ✅ Correct for ObjectId field

    applayMangerRepository.save(application); // ✅ re-save with userId

    // ✉️ Send email
    emailService.sendEmail(
        application.getEmail(),
        "Your account has been created",
        "Hello " + application.getTeamName() + ",\n\nYour account is now active.\n\n" +
                "Email: " + application.getEmail() + "\nPassword: " + randomPassword + "\n\n" +
                "Login at: https://yourapp.com/login\n\nRegards,\nEventApp Team");
}

        return ResponseEntity.ok(new ApiResponse<>(true, "Status updated", application));
    }

}
