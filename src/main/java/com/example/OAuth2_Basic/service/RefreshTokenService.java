package com.example.OAuth2_Basic.service;

import com.example.OAuth2_Basic.entity.RefreshToken;
import com.example.OAuth2_Basic.repository.RefreshTokenRepository;
import com.example.OAuth2_Basic.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final long refreshExpiration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService, @Value("${jwt.refresh-expiration}")
            long refreshExpiration) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.refreshExpiration = refreshExpiration;
    }

    public RefreshToken createRefreshToken(String username) {

        String token =
                jwtService.generateRefreshToken(username);

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setUsername(username);
        refreshToken.setToken(token);

        refreshToken.setExpiryDate(
                Instant.now().plusMillis(refreshExpiration)
        );

        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenAndRevokedFalse(token)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.getExpiryDate()
                .isBefore(Instant.now())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expired"
            );
        }

        if (!jwtService.isTokenValid(token)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid refresh token"
            );
        }

        if (!jwtService.isRefreshToken(token)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid refresh token"
            );
        }

        return refreshToken;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(
            RefreshToken oldToken) {

        oldToken.setRevoked(true);

        refreshTokenRepository.save(oldToken);

        return createRefreshToken(
                oldToken.getUsername()
        );
    }

    @Transactional
    public void revokeRefreshToken(String token) {

        refreshTokenRepository
                .findByToken(token)
                .ifPresent(refreshToken -> {

                    refreshToken.setRevoked(true);

                    refreshTokenRepository.save(refreshToken);
                });
    }
}
