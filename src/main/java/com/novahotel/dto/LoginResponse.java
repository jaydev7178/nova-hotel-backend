package com.novahotel.dto;

import com.novahotel.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private User user;
    private String message;
}

