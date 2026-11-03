package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Application.Ports.Security.ITokenGenerator;
import com.sdan.middleware.Application.Ports.Security.Permissions;
import com.sdan.middleware.Domain.Entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenGenerator implements ITokenGenerator {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_PERMISSIONS = "permissions";

    private final Key signingKey;
    private final long ttlMillis;

    public JwtTokenGenerator(
            @Value("${jwt.key.secret}") String secret,
            @Value("${jwt.key.ttl:86400000}") long ttlMillis) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("El secreto de JWT debe tener al menos 32 caracteres (256 bits)");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMillis = ttlMillis;
    }

    @Override
    public String Generate(User user) {
        if (user == null)
            throw new IllegalArgumentException("El usuario no puede ser nulo");

        Date now = new Date();
        Date expiration = new Date(now.getTime() + ttlMillis);

        List<String> defaultPermissions = Permissions.defaultUserPermissions();

        return Jwts.builder()
                .setSubject(user.getUsername().getValue())
                .claim(CLAIM_USER_ID, user.getId().toString())
                .claim(CLAIM_USERNAME, user.getUsername().getValue())
                .claim(CLAIM_PERMISSIONS, defaultPermissions)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    //Valida que el token no haya vencido
    public boolean validateToken(String token) {
        if (token == null || token.isBlank())
            return false;

        try {
            Claims claims = getClaims(token);
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public UUID extractUserId(String token) {
        Claims claims = getClaims(token);
        String userIdStr = claims.get(CLAIM_USER_ID, String.class);
        if (userIdStr == null)
            throw new IllegalArgumentException("El token no contiene el claim 'userId'");

        return UUID.fromString(userIdStr);
    }

    @Override
    public String extractUsername(String token) {
        Claims claims = getClaims(token);
        String username = claims.get(CLAIM_USERNAME, String.class);
        if (username != null) {
            return username;
        }
        return claims.getSubject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        Claims claims = getClaims(token);
        Object perms = claims.get(CLAIM_PERMISSIONS);
        if (perms instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}