package com.taivs.project.controller;

import com.taivs.project.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.taivs.project.dto.request.LoginRequest;
import com.taivs.project.dto.request.PasswordChangeRequest;
import com.taivs.project.dto.request.RefreshRequest;
import com.taivs.project.dto.request.RegisterRequest;
import com.taivs.project.dto.response.ResponseDTO;
import com.taivs.project.dto.response.UserResponseDTO;
import com.taivs.project.entity.User;
import com.taivs.project.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Login successfully")
                .data(authService.login(req)).build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseDTO> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDTO.builder().status(201).message("Refresh token successfully")
                .data(authService.refresh(req)).build());
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> logout(HttpServletRequest request) {
        System.out.println("Get into controller");
        authService.logout(request);
        return ResponseEntity.ok().body(
                ResponseDTO.builder().status(200).message("Logout successfully").build()
        );
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ResponseDTO> changePassword(@Valid @RequestBody PasswordChangeRequest req) {
        authService.changePassword(req);
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Change password successfully").build());
    }


    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> registerUser(@Valid @RequestBody RegisterRequest req) {
        if (!req.getPassword().equals(req.getRetypePassword())) {
            return ResponseEntity.badRequest().body(
                    ResponseDTO.builder()
                            .status(400)
                            .message("Passwords do not match")
                            .build()
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .status(201)
                        .message("Register successfully")
                        .data(authService.register(req))
                        .build());
    }
}

