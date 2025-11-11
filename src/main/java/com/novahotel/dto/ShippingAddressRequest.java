package com.novahotel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShippingAddressRequest {
    
    @NotBlank(message = "Street is required")
    private String street;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "Zip code is required")
    private String zipCode;
    
    // Optional fields
    private String state;
    private String country;
}