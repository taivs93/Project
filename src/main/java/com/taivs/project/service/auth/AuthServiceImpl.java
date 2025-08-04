package com.taivs.project.service.auth;

import com.taivs.project.dto.request.LoginRequest;
import com.taivs.project.dto.request.PasswordChangeRequest;
import com.taivs.project.dto.request.RefreshRequest;
import com.taivs.project.dto.request.RegisterRequest;
import com.taivs.project.dto.response.LoginResponse;
import com.taivs.project.dto.response.RefreshTokenResponse;
import com.taivs.project.dto.response.UserResponseDTO;
import com.taivs.project.entity.*;
import com.taivs.project.exception.*;
import com.taivs.project.repository.RoleRepository;
import com.taivs.project.repository.SessionRepository;
import com.taivs.project.repository.UserRepository;

import com.taivs.project.security.encryption.TokenEncryptor;
import com.taivs.project.security.jwt.JWTUtil;
import com.taivs.project.service.session.SessionService;
import com.taivs.project.service.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;


@Service

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
    private SessionRepository sessionRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenEncryptor tokenEncryptor;

    @Override
    public User getCurrentUser() {
        String tel = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByTel(tel)
                .orElseThrow(() -> new UsernameNotFoundException("Tel not found: " + tel));
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        System.out.println("start authenticating");
        authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getTel(),loginRequest.getPassword()));

        User user = userRepository.findByTel(loginRequest.getTel())
                .orElseThrow(() -> new DataNotFoundException("Tel not found"));

        System.out.println("Checking");
        sessionRepository.deleteAllByUserId(user.getId());
        System.out.println("Read session.");
        Session session = sessionService.createSession(user);

        String accessToken = tokenService.generateAccessToken(user, session.getId());
        String refreshToken = tokenService.generateRefreshToken(user,session.getId());
        System.out.println(accessToken);
        System.out.println(refreshToken);
        return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
                .userResponseDTO(UserResponseDTO.fromEntity(user)).build();
    }

    @Transactional
    public RefreshTokenResponse refresh(RefreshRequest request) {

        String rawRefreshToken = tokenEncryptor.decrypt(request.getRefreshToken());

        if(!tokenService
                .isTokenValid(rawRefreshToken)) throw new InvalidRefreshToken("Invalid refresh token");

        String tokenType = tokenService
                .extractTokenType(rawRefreshToken);
        if(!"REFRESH".equals(tokenType)) throw new InvalidTokenType("Invalid token type");

        String userId = tokenService
                .extractUserId(rawRefreshToken);
        String sessionId = tokenService
                .extractSessionId(rawRefreshToken);

        User user = userRepository.findById(Long.parseLong(userId)).orElseThrow(() -> new DataNotFoundException("User Id not found."));

        Session session = sessionRepository.findSessionById(sessionId).get();

        sessionRepository.delete(session);

        Session newSession = sessionService.createSession(user);

        String newRefreshToken = tokenService
                .generateRefreshToken(user, newSession.getId());
        String newAccessToken = tokenService
                .generateAccessToken(user, newSession.getId());

        return RefreshTokenResponse.builder().newRefreshToken(newRefreshToken).newAccessToken(newAccessToken).userResponseDTO(UserResponseDTO.fromEntity(user)).build();
    }

    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        String encryptedToken = authHeader.substring(7);
        String rawToken = tokenEncryptor.decrypt(encryptedToken);
        String sessionId = tokenService.extractSessionId(rawToken);

        sessionRepository.findSessionById(sessionId)
                .ifPresent(sessionRepository::delete);
    }


    public UserResponseDTO register(RegisterRequest req) {

        if (userRepository.existsByTel(req.getTel())) {
            throw new ResourceAlreadyExistsException("Tel already registered");
        }

        Role role = roleRepository.findByName("SHOP").orElseThrow(() -> new DataNotFoundException("Role not found"));

        User user = User.builder()
                .tel(req.getTel())
                .name(req.getName())
                .password(passwordEncoder.encode(req.getPassword()))
                .status((byte) 1)
                .address(req.getAddress())
                .build();

        UserRole userRole = UserRole.builder().user(user).role(role)
                .id(new UserRoleId(null,role.getId())).build();

        user.setUserRoles(List.of(userRole));
        userRepository.save(user);
        return UserResponseDTO.fromEntity(user);

    }

    public void changePassword(PasswordChangeRequest req) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Wrong old password");
        }

        if(req.getNewPassword().equals(req.getOldPassword())){
            throw new InvalidPasswordException("Invalid new password");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        sessionRepository.deleteAllByUserId(user.getId());
    }
}

