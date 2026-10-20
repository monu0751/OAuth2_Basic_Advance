package com.example.OAuth2_Basic.config;


import com.example.OAuth2_Basic.security.JwtService;
import com.example.OAuth2_Basic.service.CustomOidcUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOidcUserService customOidcUserService;
    private final JwtService jwtService;


    public SecurityConfig(
            CustomOidcUserService customOidcUserService, JwtService jwtService) {
        this.customOidcUserService = customOidcUserService;
        this.jwtService = jwtService;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean("keycloakJwtDecoder")
    @Primary
    public JwtDecoder keycloakJwtDecoder() {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withJwkSetUri(
                        "http://localhost:8081/realms/ecommerce/protocol/openid-connect/certs"
                ).build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        "http://localhost:8081/realms/ecommerce"
                )
        );

        return decoder;
    }

    @Bean
    @Order(0)
    public SecurityFilterChain googleAuthSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .securityMatcher("/auth/google/verify","/auth/google/token")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain customJwtSecurityFilterChain(
            HttpSecurity http,
            JwtService jwtService) throws Exception {

        http
                .securityMatcher("/auth_oidc/api/custom/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                .addFilterBefore(
                        new OncePerRequestFilter() {
                            @Override
                            protected void doFilterInternal(
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

                                String header =
                                        request.getHeader("Authorization");

                                if (header == null ||
                                        !header.startsWith("Bearer ")) {
                                    response.sendError(
                                            HttpServletResponse.SC_UNAUTHORIZED);
                                    return;
                                }

                                String token = header.substring(7);

                                if (!jwtService.isTokenValid(token)) {
                                    response.sendError(
                                            HttpServletResponse.SC_UNAUTHORIZED);
                                    return;
                                }

                                String username =
                                        jwtService.extractUsername(token);

                                Authentication authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                username,
                                                null,
                                                List.of(new SimpleGrantedAuthority(
                                                        "ROLE_USER")));

                                SecurityContextHolder.getContext()
                                        .setAuthentication(authentication);

                                filterChain.doFilter(request, response);
                            }
                        },
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain entraSecurityFilterChain(
            HttpSecurity http,
            @Qualifier("entraJwtDecoder") JwtDecoder entraJwtDecoder)
            throws Exception {

        http
                .securityMatcher("/auth/entra/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAuthority("SCOPE_access_as_user"))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(entraJwtDecoder)));

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .securityMatcher("/auth_oidc/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(keycloakJwtDecoder()))
                );

        return http.build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain browserSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/google/id-token").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                );

        return http.build();
    }
}