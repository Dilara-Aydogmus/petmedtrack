package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.config.Customizer;

import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http    .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/",
                        "/index.html",
                        "/style.css",
                        "/app.js",
                        "/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER","VET","ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/**").hasAnyRole("VET","ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("VET","ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/owners/*/activate").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("VET", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/owners/*/permanent").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasAnyRole("VET", "ADMIN")
                        .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
