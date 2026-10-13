package com.example.OAuth2_Basic.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/")
public class HomeController {

//    @GetMapping
//    public String me(@AuthenticationPrincipal OidcUser oidcUser) {
//        return "Hello " + oidcUser.getFullName() + ", email: " + oidcUser.getEmail();
//    }

    @GetMapping
    public Map<String, String> getGoogleIdToken(
            @AuthenticationPrincipal OidcUser oidcUser) {

        if (oidcUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login with Google first"
            );
        }

        return Map.of(
                "idToken", oidcUser.getIdToken().getTokenValue()
        );
    }
}
