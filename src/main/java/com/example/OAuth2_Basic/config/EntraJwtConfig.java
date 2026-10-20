package com.example.OAuth2_Basic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class EntraJwtConfig {

    @Bean("entraJwtDecoder")
    public JwtDecoder entraJwtDecoder(
            @Value("${entra.tenant-id}") String tenantId,
            @Value("${entra.api-client-id}") String clientId) {

        String issuer = "https://login.microsoftonline.com/"
                + tenantId + "/v2.0";

        NimbusJwtDecoder decoder =
                JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> defaultValidator =
                JwtValidators.createDefaultWithIssuer(issuer);



        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
                jwt.getAudience().contains(clientId)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(
                                "invalid_token",
                                "Invalid audience",
                                null
                        )
                );

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        defaultValidator,
                        audienceValidator
                )
        );

        return decoder;
    }
}
