package com.example.OAuth2_Basic.repository;

import com.example.OAuth2_Basic.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevokedFalse(
            String token);

    void deleteByUsername(String username);
}
