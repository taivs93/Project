package com.taivs.project.security.jwt;

import com.taivs.project.entity.Session;
import com.taivs.project.entity.User;
import com.taivs.project.exception.DataNotFoundException;
import com.taivs.project.exception.InvalidSessionException;
import com.taivs.project.repository.SessionRepository;
import com.taivs.project.repository.UserRepository;
import com.taivs.project.security.encryption.TokenEncryptor;
import com.taivs.project.service.auth.caching.AuthCachingService;
import com.taivs.project.service.token.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.taivs.project.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private TokenEncryptor tokenEncryptor;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthCachingService authCachingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("GET in do filter method");

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String encryptedJwt = authHeader.substring(7);
            String rawJwt = tokenEncryptor.decrypt(encryptedJwt);
            String sessionId = tokenService.extractSessionId(rawJwt);

            Long userId = authCachingService.isSessionExist(sessionId) ?
                    authCachingService.getUserIdFromSession(sessionId) :
                    Long.parseLong(tokenService.extractUserId(rawJwt));

            Session session = sessionRepository.findSessionById(sessionId).orElseThrow(() -> new DataNotFoundException("Session not found"));

            if (!session.isActive()) throw new InvalidSessionException("Invalid session id");

            if(tokenService.isTokenValid(rawJwt)
                    && "ACCESS".equals(tokenService.extractTokenType(rawJwt))
                    && tokenService.isJwtStructureValid(rawJwt)){
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new DataNotFoundException("User not found"));

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getTel());

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
