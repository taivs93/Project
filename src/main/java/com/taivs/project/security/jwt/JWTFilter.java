package com.taivs.project.security.jwt;

import com.taivs.project.entity.Session;
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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${jwt.session-expiration-ms}")
    private long durationMs;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("GET in do filter method");

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String rawJwt = tokenEncryptor.decrypt(authHeader.substring(7));
            String sessionId = tokenService.extractSessionId(rawJwt);
            if (!tokenService.isTokenValid(rawJwt) ||
                    !"ACCESS".equals(tokenService.extractTokenType(rawJwt))) {
                throw new InvalidSessionException("Invalid token");
            }
            if (!authCachingService.isSessionExist(sessionId) &&
                    !sessionRepository.existsByIdAndActive(sessionId)) {
                throw new InvalidSessionException("Invalid or expired session");
            }
            String userTel = authCachingService.getTelFromSession(sessionId);
            if (userTel == null){
                Session session = sessionRepository.findSessionById(sessionId).orElseThrow(() -> new DataNotFoundException("Session not found."));
                userTel = session.getUser().getTel();
                authCachingService.saveSession(session,this.durationMs);
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(userTel);
            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        }

        filterChain.doFilter(request, response);
    }
}
