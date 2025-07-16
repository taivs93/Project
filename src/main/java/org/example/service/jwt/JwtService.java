package org.example.service.jwt;

import io.jsonwebtoken.Claims;
import org.example.entity.Token;
import org.example.entity.User;

import java.util.Optional;
import java.util.function.Function;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String extractTel(String token);

    boolean isJwtStructureValid(String token);

    Optional<Token> findTokenInDatabase(String token);

}
