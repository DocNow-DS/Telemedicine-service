package com.healthcare.telemedicine.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final List<SecretKey> signingKeys;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.fallback-secret:}") String fallbackSecret
    ) {
        List<SecretKey> keys = new ArrayList<>();
        keys.add(buildSigningKey(secret));
        if (fallbackSecret != null && !fallbackSecret.trim().isEmpty()) {
            keys.add(buildSigningKey(fallbackSecret));
        }
        this.signingKeys = List.copyOf(keys);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object raw = claims.get("roles");
            if (raw instanceof Collection<?> collection) {
                return collection.stream().map(String::valueOf).toList();
            }
            Object singleRole = claims.get("role");
            if (singleRole instanceof String role && !role.isBlank()) {
                return List.of(role);
            }
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        for (SecretKey key : signingKeys) {
            try {
                return Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (Exception ignored) {
                // Try next configured key.
            }
        }
        throw new IllegalArgumentException("JWT signature validation failed for all configured keys");
    }

    private static SecretKey buildSigningKey(String secret) {
        String trimmed = secret == null ? "" : secret.trim();
        byte[] keyBytes = tryDecodeBase64(trimmed);
        if (keyBytes == null) {
            keyBytes = sha256(trimmed.getBytes(StandardCharsets.UTF_8));
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static byte[] tryDecodeBase64(String secret) {
        if (secret.length() < 32) return null;
        try {
            if (secret.indexOf('-') >= 0 || secret.indexOf('_') >= 0) {
                return Decoders.BASE64URL.decode(secret);
            }
            return Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create JWT signing key", e);
        }
    }
}
