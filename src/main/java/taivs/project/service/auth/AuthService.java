package taivs.project.service.auth;

import taivs.project.dto.request.PasswordChangeRequest;
import taivs.project.dto.request.RegisterRequest;
import taivs.project.entity.User;

import java.util.Map;

public interface AuthService {

    Map<String, String> login(String tel, String password);

    Map<String, String> refresh(String refreshToken, String sessionId);

    void logout(String sessionId);

    User register(RegisterRequest req);

    Map<String, String> changePassword(PasswordChangeRequest req);

    User getCurrentUser();
}
