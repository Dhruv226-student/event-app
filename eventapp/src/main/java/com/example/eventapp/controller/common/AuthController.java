package com.example.eventapp.controller.common;

import com.example.eventapp.payload.ApiResponse;
import com.example.eventapp.payload.AuthDto.AuthResponse;
import com.example.eventapp.payload.AuthDto.LoginRequest;
// import com.example.eventapp.payload.*;
import com.example.eventapp.payload.AuthDto.RegisterRequest;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.security.JwtProvider;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // Constructor injection
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            ApiResponse<User> errorResponse = new ApiResponse<>(false, "Email is already in use", null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRole("USER");

        userRepository.save(user);

        ApiResponse<User> response = new ApiResponse<>(
                true, "User registered successfully", user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, "Invalid credentials", null));
        }

        String token = jwtProvider.generateToken(user);

        // String token = JwtProvider.generateToken(user);

        AuthResponse response = new AuthResponse(
                token,
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getEmail(),
                user.getRole());

        return ResponseEntity.ok(response);
    }

}
