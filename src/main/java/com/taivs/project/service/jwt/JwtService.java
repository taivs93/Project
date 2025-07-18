package com.taivs.project.service.jwt;

import com.taivs.project.entity.Token;
import com.taivs.project.entity.User;

import java.util.Optional;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String extractTel(String token);

    boolean isJwtStructureValid(String token);

    Optional<Token> findTokenInDatabase(String token);

}
