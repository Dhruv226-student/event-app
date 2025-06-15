package com.example.eventapp.controller.userController;

import org.springframework.web.bind.annotation.RestController;

import com.example.eventapp.config.SecurityConfig.CurrentUser;
import com.example.eventapp.model.User;
import com.example.eventapp.payload.ApiResponse;
import com.example.eventapp.payload.UserDto;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/v1/user")
public class UserController {

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
        user.getRole()
    );

    ApiResponse<UserDto> response = new ApiResponse<>(true, "User details fetched", dto);
    return ResponseEntity.ok(response);
}

   
}
