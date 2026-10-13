package com.example.OAuth2_Basic.controllers;

import com.example.OAuth2_Basic.entity.EndUser;
import com.example.OAuth2_Basic.security.JwtService;
import com.example.OAuth2_Basic.service.EndUserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    private final JwtDecoder googleIdTokenDecoder;
    private final EndUserService userService;
    private final JwtService jwtService;

    public GoogleAuthController(
            @Qualifier("googleIdTokenDecoder")
            JwtDecoder googleIdTokenDecoder, EndUserService userService, JwtService jwtService) {
        this.googleIdTokenDecoder = googleIdTokenDecoder;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyGoogleToken(
            @RequestBody GoogleTokenRequest request) {

        try {
            Jwt jwt = googleIdTokenDecoder.decode(request.idToken());

            return ResponseEntity.ok(Map.of(
                    "message", "Google ID Token Validated Successfully",
                    "googleUserId", jwt.getSubject(),
                    "email", jwt.getClaimAsString("email"),
                    "name", jwt.getClaimAsString("name")
            ));

        } catch (JwtException | IllegalArgumentException ex) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid Google ID token"));
        }
    }

    @GetMapping("/id-token")
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

    @PostMapping("/token")
    public ResponseEntity<?> generateApplicationToken(
            @RequestBody GoogleTokenRequest request) {

        try {
            // 1. Validate Google ID token
            Jwt googleJwt = googleIdTokenDecoder.decode(request.idToken());

            // 2. Extract Google identity
            String googleUserId = googleJwt.getSubject();
            String email = googleJwt.getClaimAsString("email");
            String name = googleJwt.getClaimAsString("name");
            String picture = googleJwt.getClaimAsString("picture");

            // 3. Find or create local user
            EndUser user = userService.findOrCreateUser(
                    "google",
                    googleUserId,
                    email,
                    name,
                    picture
            );

            // 4. Get application role from our database
            String role = user.getRole();

            // 5. Generate OUR application JWT
            String accessToken = jwtService.generateToken(
                    user.getEmail(),
                    role
            );

            // 6. Return JWT
            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "tokenType", "Bearer"
            ));

        } catch (JwtException | IllegalArgumentException ex) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid Google ID token"));
        }
    }

    public record GoogleTokenRequest(String idToken) {}
}
