package com.example.eventapp.controller.user;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.eventapp.config.SecurityConfig.CurrentUser;
import com.example.eventapp.model.TeamApplication;
import com.example.eventapp.model.User;
import com.example.eventapp.payload.ApiResponse;
import com.example.eventapp.payload.ApplicationDto;
import com.example.eventapp.payload.AuthDto.ForgotPasswordRequest;
import com.example.eventapp.payload.AuthDto.ResetPasswordRequest;
import com.example.eventapp.payload.AuthDto.updatePassword;
import com.example.eventapp.payload.UserDto;
import com.example.eventapp.repository.ApplayMangerRepository;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.services.cloud.CloudinaryService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final CloudinaryService cloudinaryService;

    private final ApplayMangerRepository applayMangerRepository;

    private final JavaMailSender mailSender;

    UserController(ApplayMangerRepository applayMangerRepository, CloudinaryService cloudinaryService,
            PasswordEncoder passwordEncoder, UserRepository userRepository, JavaMailSender mailSender) {
        this.applayMangerRepository = applayMangerRepository;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<UserDto>> userDetails(@CurrentUser User user) {
        if (user == null) {
            ApiResponse<UserDto> error = new ApiResponse<>(false, "User not authenticated", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        UserDto dto = new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole());

        ApiResponse<UserDto> response = new ApiResponse<>(true, "User details fetched", dto);
        return ResponseEntity.ok(response);
    }

    // @PutMapping("path/{id}")
    // public ResponseEntity<ApiResponse> update(@PathVariable String id,
    // @RequestBody String entity) {
    // // TODO: process PUT request

    // // return entity;
    // }

    @PutMapping(value = "/update/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<ApiResponse<TeamApplication>> updateManager(
            @PathVariable String id,
            @ModelAttribute @Valid ApplicationDto.UpdateApplayManger request) throws IOException {

        // ✅ Find existing record
        TeamApplication existing = applayMangerRepository.findById(id).orElse(null);

        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Application not found", null));
        }

        // ❌ You were checking again for approved email — unnecessary for update
        // ✅ Optional: Keep this if needed for access control
        if (!"APPROVED".equalsIgnoreCase(existing.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Your application is not approved", null));
        }

        // ❌ Block email change
        // if (!request.getEmail().equalsIgnoreCase(existing.getEmail())) {
        // return ResponseEntity.status(HttpStatus.FORBIDDEN)
        // .body(new ApiResponse<>(false, "Email cannot be changed after submission",
        // null));
        // }

        // ✅ Handle logo update
        MultipartFile logo = request.getLogo();
        String newLogoUrl = existing.getLogoUrl();
        String newLogoPublicId = existing.getLogoPublicId();

        if (logo != null && !logo.isEmpty()) {
            // Delete old image if public_id exists
            if (newLogoPublicId != null) {
                cloudinaryService.deleteFile(newLogoPublicId);
            }

            // Upload new image
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinaryService.uploadFile(logo, "ManagerMedia");

            if (uploadResult != null && uploadResult.get("url") != null) {
                newLogoUrl = uploadResult.get("url").toString();
                newLogoPublicId = uploadResult.get("public_id").toString(); // Save this for deletion later
            }
        }

        // ✅ Update fields
        existing.setTeamName(request.getTeamName());
        // existing.setEmail(request.getEmail());
        existing.setDescription(request.getDescription());
        existing.setPortfolioUrl(request.getPortfolioUrl());
        existing.setInsta(request.getInsta());
        existing.setFacebook(request.getFacebook());
        existing.setLogoUrl(newLogoUrl);
        existing.setLogoPublicId(newLogoPublicId); // Save public ID for future deletions
        existing.setUpdatedAt(new Date());

        TeamApplication updated = applayMangerRepository.save(existing);

        return ResponseEntity.ok(new ApiResponse<>(true, "Application updated successfully", updated));
    }

    @PutMapping("change-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody updatePassword request, @CurrentUser User user) {

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<String>(false, "Old password is incorrect", null));
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<String>(false, "New password cannot be the same as the old password", null));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse<>(true, "Password updated successfully", null));
    }




    

 @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User with this email not found", null));
        }

        User user = optionalUser.get();

        // Generate reset token
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Send Email
        String subject = "Password Reset Request";
        String resetLink = "http://localhost:3000/reset-password?token=" + token; // Adjust frontend URL
        String message = "Hi " + user.getUsername() + ",\n\nClick the link below to reset your password:\n" + resetLink;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject(subject);
        email.setText(message);

        mailSender.send(email);

        return ResponseEntity.ok(new ApiResponse<>(true, "Reset email sent successfully", null));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    Optional<User> optionalUser = userRepository.findByResetToken(request.getToken());

    if (optionalUser.isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Invalid or expired token", null));
    }

    User user = optionalUser.get();

    if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Token has expired", null));
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    user.setResetToken(null);
    user.setResetTokenExpiry(null);
    userRepository.save(user);

    return ResponseEntity.ok(new ApiResponse<>(true, "Password reset successfully", null));
}

}
