package com.taivs.project.security.jwt;

import com.taivs.project.exception.InvalidTokenException;
import com.taivs.project.exception.InvalidTokenType;
import com.taivs.project.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.taivs.project.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {


    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("GET in do filter method");

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            String userIdInToken = jwtUtil.extractUserId(accessToken);
            String deviceId = jwtUtil.extractDeviceId(accessToken);
            String tokenType = jwtUtil.extractTokenType(accessToken);
            boolean isTokenBlacklist = redisTemplate.opsForValue().get("blacklist_token:" + userIdInToken + ":" + deviceId) != null;
            Object changePasswordTime = redisTemplate.opsForValue().get("TOKEN_IAT_AVL:" + userIdInToken);
            boolean validateTimeToken = (changePasswordTime == null) ||
                    jwtUtil.extractIAT(accessToken) > Long.parseLong(changePasswordTime.toString());

            if (!"ACCESS".equals(tokenType) || isTokenBlacklist
                || !validateTimeToken) throw new InvalidTokenException("Unauthorize");

            UserDetails userDetails = userDetailsService.loadUserByUsername(userIdInToken);
            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        }

        filterChain.doFilter(request, response);
    }
}
