package com.novahotel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressDto shippingAddress;
    
    private String notes;
    
    @Data
    public static class ShippingAddressDto {
        @NotNull(message = "Street is required")
        private String street;
        
        @NotNull(message = "City is required")
        private String city;
        
        @NotNull(message = "Zip code is required")
        private String zipCode;
    }
}