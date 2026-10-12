package com.example.OAuth2_Basic.controllers;

import com.example.OAuth2_Basic.dto.MeResponse;
import com.example.OAuth2_Basic.entity.EndUser;
import com.example.OAuth2_Basic.service.EndUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    private final EndUserService endUserService;

    public TestController(EndUserService endUserService) {
        this.endUserService = endUserService;
    }

    @GetMapping("/public")
    public String publicApi() {
        return "Public API";
    }

    @GetMapping("/user")
    public String userApi() {
        return "User API";
    }

    @GetMapping("/admin")
    public String adminApi() {
        return "Admin API";
    }

    @GetMapping("/me")
    public MeResponse me( @AuthenticationPrincipal OidcUser oidcUser) {
        EndUser user = endUserService.findOrCreateUser(
                "Google",
                oidcUser.getSubject(),
                oidcUser.getEmail(),
                oidcUser.getFullName(),
                oidcUser.getPicture()
        );

        return new MeResponse(
                user.getProvider(),
                user.getProviderUserId(),
                user.getEmail(),
                user.getName(),
                user.getPicture()
        );
    }

}