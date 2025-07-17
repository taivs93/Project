package taivs.project.service.auth;

import taivs.project.dto.request.PasswordChangeRequest;
import taivs.project.dto.request.RegisterRequest;
import taivs.project.entity.User;

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
