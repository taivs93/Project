package com.taivs.project.service.token;

import com.taivs.project.entity.Token;
import com.taivs.project.entity.TokenType;
import com.taivs.project.entity.User;
import com.taivs.project.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TokenServiceImpl implements TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    public void revokeAllByUser(User user) {
        List<Token> tokens = tokenRepository.findAllValidTokenByUser(user.getId());
        tokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(tokens);
    }


    public Token saveToken(User user, String tokenStr, String sessionId, TokenType type) {
        Token token = Token.builder()
                .user(user)
                .token(tokenStr)
                .revoked(false)
                .expired(false)
                .sessionId(sessionId)
                .tokenType(type)
                .build();
        return tokenRepository.save(token);
    }

    public Optional<Token> findValidToken(String refreshToken, String sessionId) {
        return tokenRepository.findByTokenAndSessionIdAndRevokedFalseAndExpiredFalse(refreshToken, sessionId);
    }

    public Optional<Token> findValidAccessTokenBySession(String sessionId) {
        return tokenRepository.findValidAccessTokenBySessionId(sessionId);
    }

    public Optional<Token> findValidRefreshTokenBySession(String sessionId) {
        return tokenRepository.findValidRefreshTokenBySessionId(sessionId);
    }

    public void revokeToken(Token token) {
        token.setRevoked(true);
        token.setExpired(true);
        tokenRepository.save(token);
    }

    public boolean isTokenValid(String token) {
        return tokenRepository
                .findByTokenAndRevokedFalseAndExpiredFalse(token)
                .isPresent();
    }

}
