package com.example.OAuth2_Basic.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/entra")
public class EntraSecurityController {

    @GetMapping("/api/secure")
    public Map<String, Object> secure(
            @AuthenticationPrincipal Jwt jwt) {

        return Map.of(
                "message", "Entra JWT authentication successful",
                "subject", jwt.getSubject(),
                "issuer", jwt.getIssuer().toString(),
                "audience", jwt.getAudience(),
                "scope", jwt.getClaimAsString("scp")
        );
    }
}
