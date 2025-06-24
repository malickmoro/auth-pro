package com.plutus.mvp.security;

import com.plutus.mvp.entity.User;
import com.plutus.mvp.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class JWTUtil {
    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);
    
    // Token expiration times
    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000L; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 days
    
    // Generate a secure secret key (in production, this should be a fixed, secure key)
    private final Key secretKey;

    public JWTUtil() {
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String generateAccessToken(User user) {
        return generateToken(user, ACCESS_TOKEN_EXPIRATION, "access");
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_TOKEN_EXPIRATION, "refresh");
    }

    private String generateToken(User user, long expirationTime, String tokenType) {
        try {
            // Current time
            long now = System.currentTimeMillis();

            // Convert roles to string list
            List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

            // Build JWT
            return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("type", tokenType)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token", e);
            throw new RuntimeException("Error generating token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.warn("Invalid JWT token", e);
            return false;
        }
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (Exception e) {
            logger.warn("Error parsing JWT token", e);
            throw new RuntimeException("Invalid token", e);
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public List<UserRole> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        List<String> roleNames = claims.get("roles", List.class);
        
        return roleNames.stream()
            .map(UserRole::valueOf)
            .collect(Collectors.toList());
    }

    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }
}