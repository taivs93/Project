package taivs.project.service.token;

import taivs.project.entity.Token;
import taivs.project.entity.TokenType;
import taivs.project.entity.User;

import java.util.Optional;

public interface TokenService {

    void revokeAllByUser(User user);

    Token saveToken(User user, String tokenStr, String sessionId, TokenType type);

    Optional<Token> findValidToken(String refreshToken, String sessionId);

    Optional<Token> findValidAccessTokenBySession(String sessionId);

    Optional<Token> findValidRefreshTokenBySession(String sessionId);

    void revokeToken(Token token);

    boolean isTokenValid(String token);
}
