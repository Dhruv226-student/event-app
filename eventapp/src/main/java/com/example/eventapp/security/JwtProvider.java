package com.example.eventapp.security;

import com.example.eventapp.model.User;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {

    private final Dotenv dotenv = Dotenv.configure()
                                        .ignoreIfMissing()
                                        .load();

    private final String jwtSecret = dotenv.get("JWT_SECRET");
    private final long jwtExpiration = Long.parseLong(dotenv.get("JWT_EXPIRATION"));

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            // Log error or throw custom auth exception
            return false;
        }
    }
}
