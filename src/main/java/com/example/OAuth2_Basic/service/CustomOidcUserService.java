package com.example.OAuth2_Basic.service;




import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final EndUserService userService;



    public CustomOidcUserService(EndUserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(
            OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        // Let Spring Security perform the normal OIDC processing
        OidcUser oidcUser = super.loadUser(userRequest);

        String provider =
                userRequest.getClientRegistration()
                        .getRegistrationId();

        String providerUserId =
                oidcUser.getSubject();

        String email =
                oidcUser.getEmail();

        String name =
                oidcUser.getFullName();

        String picture =
                oidcUser.getPicture();

        // Find or create our application user
        userService.findOrCreateUser(
                provider,
                providerUserId,
                email,
                name,
                picture
        );

        return oidcUser;
    }
}
