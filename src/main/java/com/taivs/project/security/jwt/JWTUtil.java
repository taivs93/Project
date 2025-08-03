package com.taivs.project.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String secretKeyRaw;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpMs;

    private Key key;

    @PostConstruct
    public void init() {
        if (secretKeyRaw == null) {
            System.err.println("JWT secret key not injected!");
        } else {
            System.out.println("JWT secret key injected OK");
            this.key = Keys.hmacShaKeyFor(secretKeyRaw.getBytes());
        }
    }

    public String generateAccessToken(String userId, String sessionId) {
        System.out.println("JWT Generate");
        return generateToken(userId, sessionId, "ACCESS", accessExpMs);
    }

    public String generateRefreshToken(String userId, String sessionId) {
        return generateToken(userId, sessionId, "REFRESH", refreshExpMs);
    }

    private String generateToken(String userId, String sessionId, String type, long expirationMs) {
        try {
            System.out.println("Go to generateToken method()");
            Date now = new Date();
            System.out.println("create expiryDate");
            Date expiryDate = new Date(now.getTime() + expirationMs);
            System.out.println("start build");
            String jwt = Jwts.builder()
                    .setSubject(userId)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .claim("sid", sessionId)
                    .claim("type", type)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
            System.out.println(jwt);
            return jwt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractSessionId(String token) {
        return extractAllClaims(token).get("sid", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }
}
