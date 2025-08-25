package com.taivs.project.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

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
            throw new IllegalArgumentException("JWT secret key not injected!");
        }
        this.key = Keys.hmacShaKeyFor(secretKeyRaw.getBytes());
    }

    public String generateAccessToken(String userId, String deviceId) {
        return generateToken(userId, deviceId, "ACCESS", accessExpMs);
    }

    public String generateRefreshToken(String userId, String deviceId) {
        return generateToken(userId, deviceId, "REFRESH", refreshExpMs);
    }

    private String generateToken(String userId, String deviceId, String type, long expirationMs) {
        long currentTimeMillis = System.currentTimeMillis();
        Date now = new Date(currentTimeMillis);
        Date expiryDate = new Date(currentTimeMillis + expirationMs);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .claim("iat_millis", currentTimeMillis)
                .claim("deviceId", deviceId)
                .claim("type", type)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
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

    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    public String extractDeviceId(String token) {
        return extractAllClaims(token).get("deviceId", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public long extractIAT(String token) {return extractAllClaims(token).get("iat_millis",long.class);}
}
