package com.taivs.project.service.auth;

import com.taivs.project.dto.request.*;
import com.taivs.project.dto.response.LoginResponse;
import com.taivs.project.dto.response.RefreshTokenResponse;
import com.taivs.project.dto.response.UserResponseDTO;
import com.taivs.project.entity.*;
import com.taivs.project.exception.*;
import com.taivs.project.repository.RoleRepository;
import com.taivs.project.repository.UserRepository;
import com.taivs.project.security.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private JWTUtil jwtUtil;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.session-expiration-ms}")
    private long durationMs;

    @Override
    public User getCurrentUser() {
        String tel = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByTel(tel)
                .orElseThrow(() -> new UsernameNotFoundException("Tel not found: " + tel));
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getTel(), loginRequest.getPassword())
        );

        User user = userRepository.findByTel(loginRequest.getTel())
                .orElseThrow(() -> new DataNotFoundException("Tel not found"));

        String deviceId = loginRequest.getDeviceId();
        String currentAccessToken = Objects.requireNonNull(redisTemplate.opsForValue().get("token:" + user.getId() + ":" + deviceId)).toString();
        String currentRefreshToken = Objects.requireNonNull(Objects.requireNonNull(redisTemplate.opsForValue().get("refresh:" + user.getId() + ":" + deviceId)).toString());

        redisTemplate.opsForValue().set("blacklist_token:" + user.getId() + ":" + deviceId,currentAccessToken);
        redisTemplate.opsForValue().set("blacklist_token:" + user.getId() + ":" + deviceId,currentRefreshToken);

        String accessToken = jwtUtil.generateAccessToken(user.getId().toString(), deviceId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString(), deviceId);

        redisTemplate.opsForValue().set("refresh_token:" + user.getId() + ":" + deviceId, refreshToken, refreshExpirationMs, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set("token:" + user.getId() + deviceId,accessToken,accessExpirationMs,TimeUnit.MILLISECONDS);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userResponseDTO(UserResponseDTO.fromEntity(user))
                .build();
    }

    @Override
    @Transactional
    public RefreshTokenResponse refresh(RefreshRequest request) {
        User user = this.getCurrentUser();
        String deviceId = request.getDeviceId();

        Object refreshToken = redisTemplate.opsForValue().get("refresh:" + user.getId() + ":" + deviceId);
        if (refreshToken == null) throw new DataNotFoundException("Refresh token not found");
        if (!refreshToken.toString().equals(request.getRefreshToken())) throw new InvalidRefreshToken("Invalid refresh token");

        String currentAccessToken = Objects.requireNonNull(redisTemplate.opsForValue().get("token:" + user.getId() + ":" + deviceId)).toString();
        String currentRefreshToken = Objects.requireNonNull(Objects.requireNonNull(redisTemplate.opsForValue().get("refresh:" + user.getId() + ":" + deviceId)).toString());

        redisTemplate.opsForValue().set("blacklist_token:" + user.getId() + ":" + deviceId,currentAccessToken);
        redisTemplate.opsForValue().set("blacklist_token:" + user.getId() + ":" + deviceId,currentRefreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(user.getId().toString(), deviceId);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId().toString(), deviceId);

        redisTemplate.opsForValue().set("refresh_token:" + user.getId() + ":" + deviceId, newRefreshToken, refreshExpirationMs, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set("token:" + user.getId() + deviceId,newAccessToken,accessExpirationMs,TimeUnit.MILLISECONDS);

        UserResponseDTO userResponse = UserResponseDTO.builder().tel(user.getTel())
                .name(user.getName())
                .id(user.getId())
                .address(user.getAddress())
                .status(user.getStatus())
                .build();

        return RefreshTokenResponse.builder()
                .userResponseDTO(userResponse)
                .newAccessToken(newAccessToken)
                .newRefreshToken(newRefreshToken)
                .build();
    }

    public void logout(LogoutRequestDTO request) {
        User user = this.getCurrentUser();
        String deviceId = request.getDeviceId();

        String currentAccessToken = Objects.requireNonNull(redisTemplate.opsForValue().get("token:" + user.getId() + ":" + deviceId)).toString();
        String currentRefreshToken = Objects.requireNonNull(Objects.requireNonNull(redisTemplate.opsForValue().get("refresh:" + user.getId() + ":" + deviceId)).toString());

        redisTemplate.opsForValue().set("blacklist_token:" + user.getId() + ":" + deviceId,currentAccessToken);
        redisTemplate.opsForValue().set("blacklist_token:" + user.getId() + ":" + deviceId,currentRefreshToken);
    }

    @Transactional
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

        Warehouse warehouse = Warehouse.builder()
                .name("Main warehouse")
                .user(user)
                .isMain((byte)1)
                .build();
        user.setWarehouses(List.of(warehouse));
        userRepository.save(user);

        return UserResponseDTO.fromEntity(user);

    }

    @Transactional
    public void changePassword(PasswordChangeRequest req) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(req.getOldPassword(),user.getPassword())) throw new InvalidPasswordException("Password not match");
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        redisTemplate.opsForValue().set("TOKEN_IAT_AVL:" + user.getId(), System.currentTimeMillis());
    }
}

