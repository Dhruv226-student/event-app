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
import org.apache.http.HttpStatus;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
        try {
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

            // Handle APPROVED
            if (normalizedStatus.equals("APPROVED")) {
                String randomPassword = PasswordUtil.generateRandomPassword(10);
                System.out.println("Random Password: " + randomPassword);

                User user = new User();
                user.setEmail(application.getEmail());
                user.setUsername(application.getUsername());
                user.setTeamName(application.getTeamName());
                user.setLogoUrl(application.getLogoUrl());
                user.setInsta(application.getInsta());
                user.setFacebook(application.getFacebook());
                user.setStatus("APPROVED");
                user.setPortfolioUrl(application.getPortfolioUrl());
                user.setPassword(passwordEncoder.encode(randomPassword));
                user.setRole("MANAGER");

                userRepository.save(user);

                application.setUserId(new ObjectId(user.getId()));
                applayMangerRepository.save(application);

                emailService.sendEmail(
                        application.getEmail(),
                        "Your account has been created",
                        "Hello " + application.getTeamName() + ",\n\nYour account is now active.\n\n" +
                                "Email: " + application.getEmail() + "\nPassword: " + randomPassword + "\n\n" +
                                "Login at: https://yourapp.com/login\n\nRegards,\nEventApp Team");
            }

            // Handle REJECTED after previously being APPROVED
            else if (normalizedStatus.equals("REJECTED") && application.getUserId() != null) {
                // Deactivate user if exists
                Optional<User> userOpt = userRepository.findById(application.getUserId().toHexString());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setStatus("REJECTED");
                    userRepository.save(user);

                    // Optionally notify
                    emailService.sendEmail(
                            user.getEmail(),
                            "Your account has been deactivated",
                            "Hello " + user.getTeamName() + ",\n\n" +
                                    "Your account has been deactivated as your application status is now 'REJECTED'.\n\n"
                                    +
                                    "Regards,\nEventApp Team");
                }
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Status updated", application));

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

}
