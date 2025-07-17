package taivs.project.controller;

import jakarta.validation.Valid;
import taivs.project.dto.request.LoginRequest;
import taivs.project.dto.request.PasswordChangeRequest;
import taivs.project.dto.request.RefreshRequest;
import taivs.project.dto.request.RegisterRequest;
import taivs.project.dto.response.ResponseDTO;
import taivs.project.dto.response.UserResponseDTO;
import taivs.project.entity.User;
import taivs.project.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@Valid @RequestBody LoginRequest req) {
        List<?> loginResults = authService.login(req.getTel(), req.getPassword());
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Login successfully").data(loginResults).build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseDTO> refresh(@Valid @RequestBody RefreshRequest req,@RequestHeader("X-Session-Id") String sessionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDTO.builder().status(201).message("Refresh token successfully").data(authService.refresh(req.getRefreshToken(), sessionId)).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO> logout(@RequestHeader("X-Session-Id") String sessionId) {
        authService.logout(sessionId);
        return ResponseEntity.ok().body(
                ResponseDTO.builder().status(200).message("Logout successfully").build()
        );
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ResponseDTO> changePassword(@Valid @RequestBody PasswordChangeRequest req) {
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Change password successfully").data(authService.changePassword(req)).build());
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest req) {
        if (!req.getPassword().equals(req.getRetypePassword())) {
            return ResponseEntity.badRequest().body(
                    ResponseDTO.builder()
                            .status(400)
                            .message("Passwords do not match")
                            .build()
            );
        }

        User user = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .status(201)
                        .message("Register successfully")
                        .data(UserResponseDTO.fromEntity(user))
                        .build());
    }

}

