package com.taivs.project.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.taivs.project.entity.Token;
import com.taivs.project.entity.TokenType;
import com.taivs.project.security.service.UserDetailsServiceImpl;
import com.taivs.project.service.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            Optional<Token> optionalToken = jwtService.findTokenInDatabase(jwt);
            if (optionalToken.isPresent()) {
                Token tokenEntity = optionalToken.get();

                if (!tokenEntity.isRevoked()
                        && !tokenEntity.isExpired()
                        && tokenEntity.getTokenType().equals(TokenType.ACCESS)
                        && jwtService.isJwtStructureValid(jwt)) {

                    String tel = jwtService.extractTel(jwt);
                    UserDetails user = userDetailsService.loadUserByUsername(tel);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
