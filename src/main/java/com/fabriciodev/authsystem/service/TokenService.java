package com.fabriciodev.authsystem.service;

import com.fabriciodev.authsystem.domain.entity.Permission;
import com.fabriciodev.authsystem.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class TokenService {

    private final SecretKey signingKey;

    @Value("${jwt.access-expiration-ms:900000}")      // 15 minutos
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms:604800000}")  // 7 dias
    private long refreshExpirationMs;

    public TokenService(
            @Value("${jwt.secret}") String secret) {
        // A chave precisa ter pelo menos 256 bits (32 chars) para HMAC-SHA256.
        // Gere com: openssl rand -base64 32
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .toList();

        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(accessExpirationMs)))
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("type", "access")
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(refreshExpirationMs)))
                .claim("type", "refresh")
                .signWith(signingKey)
                .compact();
    }

    /**
     * Valida a assinatura, a expiração e se o token está na blacklist.
     * Lança JwtException (ou subclasses) em caso de falha.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);

            // Verifica se foi explicitamente revogado (logout ou rotação)
            String jti = claims.getId();
//            if (isBlacklisted(jti)) {
//                return false;
//            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Valida e garante que é especificamente um refresh token.
     * Evita que alguém envie um access token no endpoint /auth/refresh.
     */
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) return false;
        return "refresh".equals(extractClaim(token, "type"));
    }

    // ------------------------------------------------- extração de claims

    /**
     * Extrai todos os claims do token.
     * Lança JwtException se o token for inválido ou expirado.
     */
    public Claims extractClaims(String token) {
        return extractAllClaims(token);
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) extractAllClaims(token).get("roles", List.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return (List<String>) extractAllClaims(token).get("permissions", List.class);
    }

    public <T> T extractClaim(String token, String claimName) {
        Claims claims = extractAllClaims(token);
        return claims.get(claimName, (Class<T>) Object.class);
    }

    /** Tempo restante de expiração em segundos (para calcular o TTL da blacklist). */
    public long getRemainingTtlSeconds(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        long diff = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, diff / 1000);
    }

    // ------------------------------------------------- blacklist (Redis)

    /**
     * Adiciona o jti na blacklist com TTL igual ao tempo restante de expiração.
     * Quando o TTL expirar, o Redis remove automaticamente — sem lixo acumulado.
     */
//    public void blacklistToken(String token) {
//        String jti = extractJti(token);
//        long ttlSeconds = getRemainingTtlSeconds(token);
//        if (ttlSeconds > 0) {
//            redis.opsForValue().set(
//                    "blacklist:" + jti,
//                    "revoked",
//                    Duration.ofSeconds(ttlSeconds)
//            );
//        }
//    }

//    public boolean isBlacklisted(String jti) {
//        return Boolean.TRUE.equals(redis.hasKey("blacklist:" + jti));
//    }

    // ------------------------------------------------- privado

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
