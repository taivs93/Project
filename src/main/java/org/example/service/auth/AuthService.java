package org.example.service.auth;

import org.example.dto.request.PasswordChangeRequest;
import org.example.dto.request.RegisterRequest;
import org.example.entity.User;

import java.util.Map;

public interface AuthService {

    Map<String, String> login(String tel, String password);

    Map<String, String> refresh(String refreshToken, String sessionId);

    void logout(String sessionId);

    User register(RegisterRequest req);

    Map<String, String> changePassword(PasswordChangeRequest req);

    User getCurrentUser();
}
