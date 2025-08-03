package com.taivs.project.service.auth;

import com.taivs.project.dto.request.LoginRequest;
import com.taivs.project.dto.request.PasswordChangeRequest;
import com.taivs.project.dto.request.RefreshRequest;
import com.taivs.project.dto.request.RegisterRequest;
import com.taivs.project.dto.response.LoginResponse;
import com.taivs.project.dto.response.RefreshTokenResponse;
import com.taivs.project.dto.response.UserResponseDTO;
import com.taivs.project.entity.User;

import java.util.List;
import java.util.Map;

public interface AuthService {

    LoginResponse login(LoginRequest req);

    RefreshTokenResponse refresh(RefreshRequest refreshRequest);

    void logout();

    UserResponseDTO register(RegisterRequest req);

    void changePassword(PasswordChangeRequest req);

    User getCurrentUser();
}
