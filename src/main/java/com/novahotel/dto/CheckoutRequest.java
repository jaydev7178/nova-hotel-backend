package com.novahotel.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CheckoutRequest {
    
    @NotNull(message = "Cart items are required")
    @NotEmpty(message = "Cart cannot be empty")
    private Map<Long, Integer> cartItems; // productId -> quantity
    
    @NotNull(message = "Shipping address is required")
    private String shippingAddress;
    
    private String notes;
}

