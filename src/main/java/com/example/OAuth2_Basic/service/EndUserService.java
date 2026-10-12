package com.example.OAuth2_Basic.service;


import com.example.OAuth2_Basic.entity.EndUser;
import com.example.OAuth2_Basic.repository.EndUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EndUserService {

    private final EndUserRepository userRepository;

    public EndUserService(EndUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public EndUser findOrCreateUser(
            String provider,
            String providerUserId,
            String email,
            String name,
            String picture) {

        return userRepository
                .findByProviderAndProviderUserId(
                        provider,
                        providerUserId
                )
                .orElseGet(() -> {

                    EndUser user = new EndUser(
                            provider,
                            providerUserId,
                            email,
                            name,
                            picture,
                            "USER"
                    );

                    return userRepository.save(user);
                });
    }
}
