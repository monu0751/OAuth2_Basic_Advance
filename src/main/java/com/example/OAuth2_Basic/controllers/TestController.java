package com.example.OAuth2_Basic.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

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
}