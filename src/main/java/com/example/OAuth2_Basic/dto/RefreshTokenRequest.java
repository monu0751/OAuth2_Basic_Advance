package com.example.OAuth2_Basic.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {

    private String refreshToken;

    public RefreshTokenRequest() {
    }
}
