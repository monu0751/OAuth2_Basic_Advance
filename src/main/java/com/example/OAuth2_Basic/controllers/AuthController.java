package com.example.OAuth2_Basic.controllers;



import com.example.OAuth2_Basic.dto.LoginRequest;
import com.example.OAuth2_Basic.dto.LoginResponse;
import com.example.OAuth2_Basic.dto.RefreshTokenRequest;
import com.example.OAuth2_Basic.entity.RefreshToken;
import com.example.OAuth2_Basic.security.JwtService;
import com.example.OAuth2_Basic.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService, UserDetailsService userDetailsService, RefreshTokenService refreshTokenService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,  HttpServletResponse response) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String role =
                userDetails.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
                        .replace("ROLE_", "");

        String accessToken =
                jwtService.generateToken(
                        userDetails.getUsername(),
                        role
                );

        String refreshToken =refreshTokenService.createRefreshToken(userDetails.getUsername()).getToken();

        ResponseCookie cookie =
                ResponseCookie.from("refreshToken", refreshToken)
                        .httpOnly(true)
                        .secure(false)
                        .path("/auth")
                        .maxAge(Duration.ofDays(7))
                        .sameSite("Strict")
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        return ResponseEntity.ok(
                new LoginResponse(
                        accessToken
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            )
            String refreshToken,  HttpServletResponse response) {



        // 1. Check refresh token exists
        if (refreshToken == null || refreshToken.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token is required"
            );
        }

        // 2. Validate against DB + JWT
        RefreshToken oldToken =
                refreshTokenService
                        .validateRefreshToken(refreshToken);



        // 4. Extract username
        String username =
                jwtService.extractUsername(refreshToken);

        // 5. Load user from database
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        // 6. Get user's current role
        String role =
                userDetails.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .map(authority ->
                                authority.replace("ROLE_", ""))
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "User role not found"
                                )
                        );

        // 7. Generate NEW access token
        String accessToken =
                jwtService.generateToken(
                        username,
                        role
                );

        // 8. Generate NEW refresh token
        RefreshToken newRefreshToken =
                refreshTokenService.rotateRefreshToken(oldToken);

        // 8. Replace cookie
        ResponseCookie cookie =
                ResponseCookie.from(
                                "refreshToken",
                                newRefreshToken.getToken()
                        )
                        .httpOnly(true)
                        .secure(false)
                        .path("/auth")
                        .maxAge(Duration.ofMillis(
                                newRefreshToken.getExpiryDate()
                                        .toEpochMilli()
                                        - Instant.now().toEpochMilli()
                        ))
                        .sameSite("Strict")
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        // 9. Return both
        return ResponseEntity.ok(
                new LoginResponse(accessToken)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            )
            String token,
            HttpServletResponse response) {

        if (token != null && !token.isBlank()) {

            refreshTokenService.revokeRefreshToken(token);
        }

        // Delete cookie
        ResponseCookie cookie =
                ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(false) // true in HTTPS production
                        .path("/auth")
                        .maxAge(0)
                        .sameSite("Strict")
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        return ResponseEntity.noContent().build();
    }
}
