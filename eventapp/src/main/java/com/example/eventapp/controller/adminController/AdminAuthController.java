package com.example.eventapp.controller.adminController;

import com.example.eventapp.model.User;
import com.example.eventapp.payload.AuthDto.AuthResponse;
import com.example.eventapp.payload.AuthDto.LoginRequest;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.security.JwtProvider;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1/admin/auth")
public class AdminAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AdminAuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, "Invalid credentials", null));
        }

        User user = optionalUser.get();

        if (!user.getRole().equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(null, null, null, "Only admins are allowed to log in here", null));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, "Invalid credentials", null));
        }

        String token = jwtProvider.generateToken(user);

        AuthResponse response = new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }
}
