package com.taivs.project.service.auth;

import com.taivs.project.dto.request.*;
import com.taivs.project.dto.response.LoginResponse;
import com.taivs.project.dto.response.RefreshTokenResponse;
import com.taivs.project.dto.response.UserResponseDTO;
import com.taivs.project.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginResponse login(LoginRequest req);

    RefreshTokenResponse refresh(RefreshRequest refreshRequest);

    void logout(LogoutRequestDTO request);

    UserResponseDTO register(RegisterRequest req);

    void changePassword(PasswordChangeRequest req);

    User getCurrentUser();
}
