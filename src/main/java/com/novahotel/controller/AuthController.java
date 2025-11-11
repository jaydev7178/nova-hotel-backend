package com.novahotel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.novahotel.dto.LoginRequest;
import com.novahotel.dto.LoginResponse;
import com.novahotel.dto.RegisterRequest;
import com.novahotel.entity.User;
import com.novahotel.security.JwtUtil;
import com.novahotel.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setFullName(request.getFullName());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setAddress(request.getAddress());
            
            User savedUser = userService.registerUser(user);

            // Optionally generate a JWT for the newly registered user to auto-login
            String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole().name());

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUser(savedUser);
            loginResponse.setMessage("Registration successful");

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "User registered successfully", loginResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
            System.out.println(passwordEncoder.encode(request.getPassword()).toString());
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = (User) authentication.getPrincipal();
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUser(user);
            response.setMessage("Login successful");
            
            return ResponseEntity.ok(new ApiResponse(true, "Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid credentials", null));
        }
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok(new ApiResponse(true, "User info retrieved", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Unable to get user info", null));
        }
    }
    
    // DTOs
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;
        
        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}

