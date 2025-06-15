package com.example.eventapp.seeder;


import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserSeeder implements CommandLineRunner{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

  
 @Override
public void run(String... args) {
    try {
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = User.builder()
                    .username("Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Admin user created");
        } else {
            System.out.println("ℹ️ Admin user already exists");
        }
    } catch (Exception e) {
        System.err.println("❌ Failed to seed admin user: " + e.getMessage());
        e.printStackTrace();
    }
}
}
