package com.taivs.project.service.token;

import com.taivs.project.entity.User;


public interface TokenService {

    boolean isTokenValid(String token);

    String generateAccessToken(User user, String sessionId);

    String generateRefreshToken(User user, String sessionId);

    String extractUserId(String token);

    String extractSessionId(String token);

    String extractTokenType(String token);

    boolean isJwtStructureValid(String token);
}

