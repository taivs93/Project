package com.taivs.project.service.token;

import com.taivs.project.entity.User;
import com.taivs.project.security.encryption.TokenEncryptor;
import com.taivs.project.security.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private TokenEncryptor encryptor;

    @Override
    public boolean isTokenValid(String token) {
        return jwtUtil.isTokenValid(token);
    }

    @Override
    public String generateAccessToken(User user, String sessionId) {
        System.out.println("Generate token");
        String rawToken = jwtUtil.generateAccessToken(user.getId().toString(),sessionId);
        System.out.println(rawToken);
        return encryptor.encrypt(rawToken);
    }

    @Override
    public String generateRefreshToken(User user, String sessionId) {
        String rawRefreshToken = jwtUtil.generateRefreshToken(user.getId().toString(), sessionId);
        return encryptor.encrypt(rawRefreshToken);
    }

    @Override
    public String extractUserId(String token) {
        return jwtUtil.extractUserId(token);
    }

    @Override
    public String extractSessionId(String token) {
        return jwtUtil.extractSessionId(token);
    }

    @Override
    public String extractTokenType(String token) {
        return jwtUtil.extractTokenType(token);
    }

    @Override
    public boolean isJwtStructureValid(String token) {
        try {
            jwtUtil.extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
