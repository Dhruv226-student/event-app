package com.example.eventapp.security;

import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public JwtFilter(JwtProvider jwtProvider, UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            System.out.println(token);
            try {
                if (jwtProvider.validateToken(token)) {
                    String email = jwtProvider.getEmailFromToken(token);
                    User user = userRepository.findByEmail(email).orElse(null);

                    if (user != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                user, null, Collections.emptyList());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (JwtException e) {
                // You can optionally log or handle token errors here
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
