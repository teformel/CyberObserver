package com.cyber.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for WebSocket/API focus (or configure properly if needed later)
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                // CSP: Allow scripts from self and unpkg (Three.js CDN)
                .addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy", 
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://unpkg.com; " + // unsafe-eval/inline needed for some Three.js examples/module loading
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; " +
                    "connect-src 'self' ws://localhost:8080 ws://*:8080;"
                ))
                // Prevent Clickjacking
                .frameOptions(frame -> frame.deny())
            )
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Allow all for now, as we use internal Auth key logic
            );
        return http.build();
    }
}
