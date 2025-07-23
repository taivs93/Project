package com.taivs.project.service.auth;

import com.taivs.project.dto.request.PasswordChangeRequest;
import com.taivs.project.dto.request.RegisterRequest;
import com.taivs.project.dto.response.UserResponseDTO;
import com.taivs.project.entity.*;
import com.taivs.project.exception.*;
import com.taivs.project.repository.RoleRepository;
import com.taivs.project.repository.UserRepository;
import com.taivs.project.service.jwt.JwtService;
import com.taivs.project.service.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public User getCurrentUser() {
        String tel = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByTel(tel)
                .orElseThrow(() -> new UsernameNotFoundException("Tel not found: " + tel));
    }

    private Map<String, String> revokeAndGenerateNewToken(User user) {
        tokenService.revokeAllByUser(user);

        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        tokenService.saveToken(user, accessToken, sessionId, TokenType.ACCESS);
        tokenService.saveToken(user, refreshToken, sessionId, TokenType.REFRESH);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "sessionId", sessionId,
                "tokenType", "Bearer"
        );
    }

    public List<?> login(String tel, String password) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(tel, password));
        User user = userRepository.findByTel(tel)
                .orElseThrow(() -> new UsernameNotFoundException("Tel not found:" + tel));
        return List.of(revokeAndGenerateNewToken(user), UserResponseDTO.fromEntity(user));
    }


    public Map<String, String> refresh(String refreshToken, String sessionId) {
        Token token = tokenService.findValidToken(refreshToken, sessionId)
                .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));

        User user = token.getUser();
        if (user.getStatus() != 1) throw new UserInactiveException("User is invalid");
        return revokeAndGenerateNewToken(user);
    }

    public void logout(String sessionId) {
        Token accessToken = tokenService.findValidAccessTokenBySession(sessionId)
                .orElseThrow(() -> new DataNotFoundException("Session not found"));
        Token refreshToken = tokenService.findValidRefreshTokenBySession(sessionId)
                        .orElseThrow(() -> new DataNotFoundException("Session not found"));
        tokenService.revokeToken(accessToken);
        tokenService.revokeToken(refreshToken);
    }

    public User register(RegisterRequest req) {
        if (userRepository.existsByTel(req.getTel())) {
            throw new ResourceAlreadyExistsException("Tel already registered");
        }
        if (userRepository.existsByName(req.getName())) {
            throw new ResourceAlreadyExistsException("Name already registered");
        }
        Role role = roleRepository.findByName("USER").orElseThrow(() -> new DataNotFoundException("Role not found"));
        User user = User.builder()
                .tel(req.getTel())
                .name(req.getName())
                .password(passwordEncoder.encode(req.getPassword()))
                .status((byte) 1)
                .address(req.getAddress())
                .build();
        userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setId(new UserRoleId(user.getId(), role.getId()));
        user.setUserRoles(List.of(userRole));
        return user;
    }
    public List<?> changePassword(PasswordChangeRequest req) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Wrong old password");
        }
        if(req.getNewPassword().equals(req.getOldPassword())){
            throw new InvalidPasswordException("Invalid new password");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return login(user.getTel(), req.getNewPassword());
    }
}

