package com.example.OAuth2_Basic.repository;


import com.example.OAuth2_Basic.entity.EndUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EndUserRepository
        extends JpaRepository<EndUser, Long> {

    Optional<EndUser> findByProviderAndProviderUserId(
            String provider,
            String providerUserId
    );

    Optional<EndUser> findByEmail(String email);
}
