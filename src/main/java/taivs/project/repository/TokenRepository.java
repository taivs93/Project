package taivs.project.repository;

import taivs.project.entity.Token;
import taivs.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenAndRevokedFalseAndExpiredFalse(String token);

    Optional<Token> findByTokenAndSessionIdAndRevokedFalseAndExpiredFalse(String token, String sessionId);

    @Query("SELECT t FROM Token t WHERE t.sessionId = :sessionId AND t.revoked = false AND t.expired = false AND t.tokenType = taivs.project.entity.TokenType.REFRESH")
    Optional<Token> findValidRefreshTokenBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT t FROM Token t WHERE t.sessionId = :sessionId AND t.revoked = false AND t.expired = false AND t.tokenType = taivs.project.entity.TokenType.ACCESS")
    Optional<Token> findValidAccessTokenBySessionId(@Param("sessionId") String sessionId);

    @Modifying
    @Query("UPDATE Token t SET t.expired = true, t.revoked = true WHERE t.user = :user AND (t.expired = false OR t.revoked = false)")
    void revokeAllByUser(@Param("user") User user);

    @Query("SELECT t FROM Token t WHERE t.user.id = :userId AND t.expired = false AND t.revoked = false")
    List<Token> findAllValidTokenByUser(Long userId);

    Optional<Token> findByToken(String token);
}
