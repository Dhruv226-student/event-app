package com.example.eventapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableMongoAuditing
@EnableMethodSecurity(prePostEnabled = true) // 👈 This is required for @PreAuthorize to work

public class EventappApplication {

	 public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(EventappApplication.class, args);
    }

}
