package com.example.eventapp.controller.admin;

import com.example.eventapp.config.SecurityConfig.CurrentUser;
import com.example.eventapp.model.User;
import com.example.eventapp.payload.ApiResponse;
import com.example.eventapp.payload.AuthDto.AuthResponse;
import com.example.eventapp.payload.AuthDto.ForgotPasswordRequest;
import com.example.eventapp.payload.AuthDto.LoginRequest;
import com.example.eventapp.payload.AuthDto.ResetPasswordRequest;
import com.example.eventapp.payload.AuthDto.updatePassword;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.security.JwtProvider;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/v1/admin/auth")
public class AdminAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JavaMailSender mailSender;

    public AdminAuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.mailSender = mailSender;
    }

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest request) {
    Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

    if (optionalUser.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<AuthResponse>(false, "Invalid credentials", null));
    }

    User user = optionalUser.get();

    if (!user.getRole().equalsIgnoreCase("ADMIN")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<AuthResponse>(false, "Only admins are allowed to log in here", null));
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<AuthResponse>(false, "Invalid credentials", null));
    }

    String token = jwtProvider.generateToken(user);
    AuthResponse authResponse = new AuthResponse(
            token,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole()
    );

    return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", authResponse));
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
