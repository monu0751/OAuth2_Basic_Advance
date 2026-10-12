package com.example.OAuth2_Basic.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String me(@AuthenticationPrincipal OidcUser oidcUser) {
        return "Hello " + oidcUser.getFullName() + ", email: " + oidcUser.getEmail();
    }
}
