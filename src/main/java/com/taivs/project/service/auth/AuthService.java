package com.taivs.project.service.auth;

import com.taivs.project.dto.request.PasswordChangeRequest;
import com.taivs.project.dto.request.RegisterRequest;
import com.taivs.project.entity.User;

import java.util.List;
import java.util.Map;

public interface AuthService {

    List<?> login(String tel, String password);

    Map<String, String> refresh(String refreshToken, String sessionId);

    void logout(String sessionId);

    User register(RegisterRequest req);

    List<?> changePassword(PasswordChangeRequest req);

    User getCurrentUser();
}
