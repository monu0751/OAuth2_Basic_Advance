package com.example.OAuth2_Basic.dto;

public record MeResponse(
        String provider,
        String providerUserId,
        String email,
        String name,
        String picture
) {
}