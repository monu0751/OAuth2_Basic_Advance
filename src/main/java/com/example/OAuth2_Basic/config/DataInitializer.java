package com.example.OAuth2_Basic.config;

import com.example.OAuth2_Basic.entity.User;
import com.example.OAuth2_Basic.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (userRepository.findByUsername("abhinav").isEmpty()) {

                User user = new User(
                        "abhinav",
                        passwordEncoder.encode("password123"),
                        "USER"
                );

                userRepository.save(user);
            }

            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "ADMIN"
                );

                userRepository.save(admin);
            }
        };
    }
}
