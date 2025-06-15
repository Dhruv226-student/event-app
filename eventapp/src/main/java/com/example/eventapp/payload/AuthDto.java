package com.example.eventapp.payload;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Container class – make it non‑public so inner DTOs can be public.
 */
public class AuthDto {           // <─ file must be named AuthDtos.java

    @Data
    public static class LoginRequest {
        @Email @NotBlank String email;
        @NotBlank              String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank             String username;
        @Email @NotBlank      String email;
        @Size(min = 6)        String password;
    }

    @Data @AllArgsConstructor
    public static class AuthResponse {
        String token;
        String id;
        String username;
        String email;
        String role;
    }
}
