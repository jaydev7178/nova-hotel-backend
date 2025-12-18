package com.novahotel.dto;


import lombok.Data;

@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String firstName;
    private String lastName;
    private String role;
}
