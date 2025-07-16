package org.example.service.jwt;

import io.jsonwebtoken.Claims;
import org.example.entity.Token;
import org.example.entity.User;
import org.example.repository.TokenRepository;
import org.example.security.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private TokenRepository tokenRepository;

    public String generateAccessToken(User user) {
        return jwtUtil.generateAccessToken(user.getTel());
    }

    public String generateRefreshToken(User user) {
        return jwtUtil.generateRefreshToken(user.getTel());
    }

    public String extractTel(String token) {
        return jwtUtil.extractTel(token);
    }
    public boolean isJwtStructureValid(String token) {
        return jwtUtil.isTokenValid(token);
    }

    public Optional<Token> findTokenInDatabase(String token) {
        return tokenRepository.findByToken(token);
    }

}
