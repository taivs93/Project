package taivs.project.service.jwt;

import taivs.project.entity.Token;
import taivs.project.entity.User;
import taivs.project.repository.TokenRepository;
import taivs.project.security.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
