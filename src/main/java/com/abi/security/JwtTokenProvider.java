package com.abi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey signingKey;
    private final String issuer;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${app.security.jwt.secret-key}") final String secretKey,
            @Value("${app.security.jwt.issuer}") final String issuer,
            @Value("${app.security.jwt.access-token-expiration-ms}") final long accessTokenExpirationMs,
            @Value("${app.security.jwt.refresh-token-expiration-ms}") final long refreshTokenExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(HexFormat.of().parseHex(secretKey));
        this.issuer = issuer;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(final UserDetails userDetails) {
        return buildToken(userDetails, accessTokenExpirationMs);
    }

    public String generateRefreshToken(final UserDetails userDetails) {
        return buildToken(userDetails, refreshTokenExpirationMs);
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    public String extractUsername(final String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(final String token) {
        try {
            final Claims claims = parseClaims(token);
            return claims.getExpiration().after(Date.from(Instant.now()));
        } catch (final JwtException | IllegalArgumentException ex) {
            LOGGER.warn("JWT-DEBUG token invalid: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            return false;
        }
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(final UserDetails userDetails, final long expirationMs) {
        final Instant now = Instant.now();
        final String roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .claim("roles", roles)
                .signWith(signingKey)
                .compact();
    }
}
