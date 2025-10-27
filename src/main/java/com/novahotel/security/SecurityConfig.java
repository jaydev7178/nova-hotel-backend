package com.novahotel.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, com.novahotel.service.UserService userService) {
        return new JwtAuthenticationFilter(jwtUtil, userService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                // Test endpoints
                .requestMatchers("/test/**").permitAll()  // Removed /api
                
                // Swagger UI endpoints
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                
                // Public endpoints
                .requestMatchers(
                    "/error",
                    "/favicon.ico",
                    "/actuator/health"
                ).permitAll()
                
                // Auth endpoints - SPECIFIC RULES FIRST!
                .requestMatchers(
                    "/auth/register",     // Removed /api
                    "/auth/login"         // Removed /api
                ).permitAll()
                
                // Protected auth endpoints
                .requestMatchers("/auth/**").authenticated()  // Removed /api
                
                // Public content endpoints
                .requestMatchers(
                    "/products/**",       // Removed /api
                    "/categories/**"      // Removed /api
                ).permitAll()
                
                // User endpoints
                .requestMatchers(
                    "/users/**",          // Removed /api
                    "/cart/**",           // Removed /api
                    "/orders/**"          // Removed /api
                ).hasAnyRole("USER", "OWNER", "ADMIN")
                
                // Admin endpoints
                .requestMatchers(
                    "/admin/**",          // Removed /api
                    "/owner/**"           // Removed /api
                ).hasAnyRole("OWNER", "ADMIN")
                
                // Everything else needs authentication
                .anyRequest().authenticated()
            );
            
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

