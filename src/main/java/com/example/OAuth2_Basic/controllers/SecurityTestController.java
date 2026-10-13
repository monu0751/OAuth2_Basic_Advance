package com.example.OAuth2_Basic.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth_oidc")
public class SecurityTestController {

//    @GetMapping("/api/secure")
//    public String secure(@AuthenticationPrincipal OidcUser oidcUser) {
//        return "Hello " + oidcUser.getFullName() + ", email: " + oidcUser.getEmail();
//    }

//    @GetMapping("/api/secure")
//    public String secure(@AuthenticationPrincipal Jwt jwt) {
//
//        String username = jwt.getClaimAsString("preferred_username");
//        String email = jwt.getClaimAsString("email");
//
//        return "Hello " + username + ", email: " + email;
//    }

    @GetMapping("/api/secure")
    public Map<String, Object> secure(
            @AuthenticationPrincipal Jwt jwt) {

        return Map.of(
                "message", "JWT authentication successful",
                "username", jwt.getClaimAsString("preferred_username"),
                "subject", jwt.getSubject(),
                "issuer", jwt.getIssuer().toString()
        );
    }

    @GetMapping("/api/custom/secure")
    public Object secure(Authentication authentication) {
        return authentication.getPrincipal();
    }


}
