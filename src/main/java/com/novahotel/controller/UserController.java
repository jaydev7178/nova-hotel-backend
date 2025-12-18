package com.novahotel.controller;

import com.novahotel.dto.UserProfileDTO;
import com.novahotel.entity.User;
import com.novahotel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management endpoints")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    @Transactional
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(Authentication authentication) {
    	User user = (User) authentication.getPrincipal();

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole().name());

        return ResponseEntity.ok(dto);

    }
    
    @PutMapping("/update-profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody User user, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        User updatedUser = userService.updateUser(currentUser.getId(), user);
        return ResponseEntity.ok(updatedUser);
    }
}

